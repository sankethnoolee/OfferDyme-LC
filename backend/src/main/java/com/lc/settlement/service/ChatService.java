package com.lc.settlement.service;

import com.lc.settlement.dto.*;
import com.lc.settlement.entity.*;
import com.lc.settlement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Owns the conversation flow for both ACCOUNT-level and CUSTOMER / PORTFOLIO-level chats.
 *
 * Turn-based flow:
 *   1) Agent enters ONLY the customer's utterance in the UI
 *   2) ChatService appends that as a CUSTOMER message
 *   3) Calls Claude to get sentiment, summary, recommended reply, and settlement range
 *   4) Auto-appends Claude's recommended reply as an AGENT message
 *   5) Snapshots transcript history and returns the full transcript + the decision
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final TranscriptRepository transcriptRepo;
    private final ChatMessageRepository messageRepo;
    private final TranscriptHistoryRepository historyRepo;
    private final AccountRepository accountRepo;
    private final CustomerRepository customerRepo;
    private final FieldAgentRepository agentRepo;
    private final ClaudeService claudeService;

    // ----------------------------------------------------------------
    // Read APIs
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TranscriptDto> listForAccount(Long accountId) {
        return transcriptRepo.findByAccountIdOrderByStartedAtDesc(accountId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TranscriptDto> listForCustomer(Long customerId) {
        return transcriptRepo.findByCustomerIdOrderByStartedAtDesc(customerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TranscriptDto> listPortfolioForCustomer(Long customerId) {
        return transcriptRepo.findByCustomerIdAndAccountIsNullOrderByStartedAtDesc(customerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TranscriptDto getTranscript(Long transcriptId) {
        Transcript t = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> new IllegalArgumentException("Transcript not found: " + transcriptId));
        return toDto(t);
    }

    @Transactional(readOnly = true)
    public List<TranscriptHistoryDto> history(Long transcriptId) {
        return historyRepo.findByTranscriptIdOrderByCapturedAtAsc(transcriptId)
                .stream().map(this::toHistoryDto).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // Turn-based endpoints (auto-generate agent reply)
    // ----------------------------------------------------------------

    /**
     * Submit one customer utterance on an account-level chat, then let Claude
     * produce the agent's next reply (which is auto-appended as an AGENT message).
     */
    @Transactional
    public TurnResultDto takeAccountTurn(SendMessageRequest req) {
        if (req.getAccountId() == null) {
            throw new IllegalArgumentException("accountId is required for account-level turn");
        }
        Account account = accountRepo.findById(req.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + req.getAccountId()));
        FieldAgent agent = resolveAgent(req.getAgentId(), account.getAssignedAgent());

        Transcript transcript = transcriptRepo
                .findFirstByAccountIdAndStatusOrderByStartedAtDesc(account.getId(), "OPEN")
                .orElseGet(() -> createOpenAccountTranscript(account, agent));

        return runTurn(transcript, account.getCustomer(), List.of(account), agent, req.getContent(), false);
    }

    /**
     * Submit one customer utterance on a customer-level (portfolio) chat.
     * Claude is given ALL of the customer's accounts.
     */
    @Transactional
    public TurnResultDto takeCustomerTurn(SendMessageRequest req) {
        if (req.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId is required for portfolio turn");
        }
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + req.getCustomerId()));

        // Portfolios only make sense within a single lender — scope down if lenderId is set.
        List<Account> accounts = req.getLenderId() == null
                ? accountRepo.findByCustomerId(customer.getId())
                : accountRepo.findByCustomerIdAndLenderId(customer.getId(), req.getLenderId());

        FieldAgent agent = resolveAgent(req.getAgentId(),
                accounts.isEmpty() ? null : accounts.get(0).getAssignedAgent());

        Transcript transcript = transcriptRepo
                .findFirstByCustomerIdAndAccountIsNullAndStatusOrderByStartedAtDesc(customer.getId(), "OPEN")
                .orElseGet(() -> createOpenPortfolioTranscript(customer, agent));

        return runTurn(transcript, customer, accounts, agent, req.getContent(), true);
    }

    // ----------------------------------------------------------------
    // Core turn logic
    // ----------------------------------------------------------------

    private TurnResultDto runTurn(Transcript transcript, Customer customer, List<Account> accounts,
                                  FieldAgent agent, String customerUtterance, boolean portfolio) {

        String trimmed = customerUtterance == null ? "" : customerUtterance.trim();
        if (!trimmed.isEmpty()) {
            ChatMessage customerMsg = ChatMessage.builder()
                    .transcript(transcript)
                    .senderType("CUSTOMER")
                    .senderName(customer.getFullName())
                    .content(trimmed)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepo.save(customerMsg);
        }

        List<ChatMessage> msgs = messageRepo.findByTranscriptIdOrderByCreatedAtAsc(transcript.getId());

        ClaudeDecisionDto decision = portfolio
                ? claudeService.analyzePortfolio(customer, accounts, transcript, msgs)
                : claudeService.analyze(customer, accounts.get(0), transcript, msgs);

        // Stamp the last customer message with the detected sentiment.
        for (int i = msgs.size() - 1; i >= 0; i--) {
            if ("CUSTOMER".equals(msgs.get(i).getSenderType())) {
                msgs.get(i).setSentiment(decision.getSentiment());
                msgs.get(i).setSentimentScore(decision.getSentimentScore());
                messageRepo.save(msgs.get(i));
                break;
            }
        }

        // Auto-record Claude's proposed reply as an AGENT message.
        String proposedReply = decision.getProposedReply();
        if (proposedReply != null && !proposedReply.isBlank()) {
            ChatMessage agentMsg = ChatMessage.builder()
                    .transcript(transcript)
                    .senderType("AGENT")
                    .senderName(agent != null ? agent.getName() : "LC Agent")
                    .content(proposedReply)
                    .sentiment(decision.getSentiment())
                    .sentimentScore(decision.getSentimentScore())
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepo.save(agentMsg);
        }

        // Persist overall sentiment/summary on the transcript.
        transcript.setSentiment(decision.getSentiment());
        transcript.setSentimentScore(decision.getSentimentScore());
        transcript.setSummary(decision.getSummary());
        transcriptRepo.save(transcript);

        snapshotHistory(transcript, "TURN_COMPLETED");

        Transcript reloaded = transcriptRepo.findById(transcript.getId()).orElseThrow();
        return TurnResultDto.builder()
                .transcript(toDto(reloaded))
                .decision(decision)
                .build();
    }

    /**
     * Legacy raw-append endpoint, still useful for manually inserting an AGENT
     * message (e.g. editing the suggested reply before sending).
     */
    @Transactional
    public TranscriptDto appendMessage(SendMessageRequest req) {
        if (req.getAccountId() == null && req.getCustomerId() == null) {
            throw new IllegalArgumentException("accountId or customerId is required");
        }

        Transcript transcript;
        Customer customer;
        if (req.getAccountId() != null) {
            Account account = accountRepo.findById(req.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + req.getAccountId()));
            customer = account.getCustomer();
            FieldAgent agent = resolveAgent(req.getAgentId(), account.getAssignedAgent());
            transcript = transcriptRepo
                    .findFirstByAccountIdAndStatusOrderByStartedAtDesc(account.getId(), "OPEN")
                    .orElseGet(() -> createOpenAccountTranscript(account, agent));
        } else {
            customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + req.getCustomerId()));
            FieldAgent agent = resolveAgent(req.getAgentId(), null);
            transcript = transcriptRepo
                    .findFirstByCustomerIdAndAccountIsNullAndStatusOrderByStartedAtDesc(customer.getId(), "OPEN")
                    .orElseGet(() -> createOpenPortfolioTranscript(customer, agent));
        }

        String senderType = req.getSenderType() == null ? "CUSTOMER" : req.getSenderType().toUpperCase();
        String senderName = "AGENT".equals(senderType)
                ? (transcript.getFieldAgent() != null ? transcript.getFieldAgent().getName() : "LC Agent")
                : customer.getFullName();

        ChatMessage msg = ChatMessage.builder()
                .transcript(transcript)
                .senderType(senderType)
                .senderName(senderName)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        messageRepo.save(msg);

        snapshotHistory(transcript, "MESSAGE_ADDED");
        return toDto(transcriptRepo.findById(transcript.getId()).orElseThrow());
    }

    /** Re-run Claude on an existing transcript without adding a new turn. */
    @Transactional
    public ClaudeDecisionDto analyze(Long transcriptId) {
        Transcript transcript = transcriptRepo.findById(transcriptId)
                .orElseThrow(() -> new IllegalArgumentException("Transcript not found: " + transcriptId));

        List<ChatMessage> msgs = messageRepo.findByTranscriptIdOrderByCreatedAtAsc(transcriptId);

        ClaudeDecisionDto decision;
        if (transcript.getAccount() == null) {
            List<Account> accounts = accountRepo.findByCustomerId(transcript.getCustomer().getId());
            decision = claudeService.analyzePortfolio(transcript.getCustomer(), accounts, transcript, msgs);
        } else {
            decision = claudeService.analyze(
                    transcript.getCustomer(),
                    transcript.getAccount(),
                    transcript,
                    msgs);
        }

        transcript.setSentiment(decision.getSentiment());
        transcript.setSentimentScore(decision.getSentimentScore());
        transcript.setSummary(decision.getSummary());
        transcriptRepo.save(transcript);

        for (int i = msgs.size() - 1; i >= 0; i--) {
            if ("CUSTOMER".equals(msgs.get(i).getSenderType())) {
                msgs.get(i).setSentiment(decision.getSentiment());
                msgs.get(i).setSentimentScore(decision.getSentimentScore());
                messageRepo.save(msgs.get(i));
                break;
            }
        }

        snapshotHistory(transcript, "CLAUDE_ANALYZED");
        return decision;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private FieldAgent resolveAgent(Long requestedId, FieldAgent fallback) {
        if (requestedId != null) {
            return agentRepo.findById(requestedId).orElse(fallback);
        }
        return fallback;
    }

    private Transcript createOpenAccountTranscript(Account account, FieldAgent agent) {
        Transcript t = Transcript.builder()
                .customer(account.getCustomer())
                .account(account)
                .portfolioLevel(false)
                .fieldAgent(agent)
                .channel("CHAT")
                .startedAt(LocalDateTime.now())
                .status("OPEN")
                .sentiment("NEUTRAL")
                .sentimentScore(0.0)
                .build();
        return transcriptRepo.save(t);
    }

    private Transcript createOpenPortfolioTranscript(Customer customer, FieldAgent agent) {
        Transcript t = Transcript.builder()
                .customer(customer)
                .account(null)
                .portfolioLevel(true)
                .fieldAgent(agent)
                .channel("CHAT")
                .startedAt(LocalDateTime.now())
                .status("OPEN")
                .sentiment("NEUTRAL")
                .sentimentScore(0.0)
                .build();
        return transcriptRepo.save(t);
    }

    private void snapshotHistory(Transcript t, String reason) {
        List<ChatMessage> msgs = messageRepo.findByTranscriptIdOrderByCreatedAtAsc(t.getId());
        String fullText = msgs.stream()
                .map(m -> "[" + m.getCreatedAt() + "] " + m.getSenderType()
                          + " (" + (m.getSenderName() == null ? "" : m.getSenderName()) + "): " + m.getContent())
                .collect(Collectors.joining(" | "));

        TranscriptHistory h = TranscriptHistory.builder()
                .transcript(t)
                .fullText(fullText)
                .sentiment(t.getSentiment())
                .sentimentScore(t.getSentimentScore())
                .summary(t.getSummary())
                .changeReason(reason)
                .capturedAt(LocalDateTime.now())
                .build();
        historyRepo.save(h);
    }

    private TranscriptDto toDto(Transcript t) {
        List<ChatMessage> msgs = messageRepo.findByTranscriptIdOrderByCreatedAtAsc(t.getId());
        return TranscriptDto.builder()
                .id(t.getId())
                .customerId(t.getCustomer().getId())
                .accountId(t.getAccount() == null ? null : t.getAccount().getId())
                .portfolioLevel(Boolean.TRUE.equals(t.getPortfolioLevel()) || t.getAccount() == null)
                .channel(t.getChannel())
                .startedAt(t.getStartedAt())
                .endedAt(t.getEndedAt())
                .sentiment(t.getSentiment())
                .sentimentScore(t.getSentimentScore())
                .summary(t.getSummary())
                .status(t.getStatus())
                .fieldAgentName(t.getFieldAgent() == null ? null : t.getFieldAgent().getName())
                .messages(msgs.stream().map(this::toMessageDto).collect(Collectors.toList()))
                .build();
    }

    private ChatMessageDto toMessageDto(ChatMessage m) {
        return ChatMessageDto.builder()
                .id(m.getId())
                .senderType(m.getSenderType())
                .senderName(m.getSenderName())
                .content(m.getContent())
                .sentiment(m.getSentiment())
                .sentimentScore(m.getSentimentScore())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private TranscriptHistoryDto toHistoryDto(TranscriptHistory h) {
        return TranscriptHistoryDto.builder()
                .id(h.getId())
                .fullText(h.getFullText())
                .sentiment(h.getSentiment())
                .sentimentScore(h.getSentimentScore())
                .summary(h.getSummary())
                .changeReason(h.getChangeReason())
                .capturedAt(h.getCapturedAt())
                .build();
    }
}
