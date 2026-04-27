package com.lc.settlement.dto;

import lombok.*;

/**
 * Request body for a single "turn" in the conversation.
 *
 * Two flavors:
 *   1) account-level turn: accountId is set, customerId may be null
 *   2) portfolio-level turn: customerId is set, accountId is null
 *
 * The agent in the UI only types the CUSTOMER's utterance. The backend
 * will append it to the open transcript, call Claude, and record the
 * recommended agent reply as an AGENT message automatically.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SendMessageRequest {
    private Long accountId;
    private Long customerId;
    private Long agentId;
    /** Scopes a portfolio / customer turn to accounts of this lender only. */
    private Long lenderId;
    /** Optional; defaults to CUSTOMER because the UI only captures customer utterances. */
    private String senderType;
    private String content;
}
