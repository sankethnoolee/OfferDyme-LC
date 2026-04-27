package com.lc.settlement.dto;

import lombok.*;

/**
 * The bundled response returned after a single turn in the conversation:
 *   - the full, up-to-date transcript (customer utterance + auto-recorded agent reply)
 *   - Claude's decision for the right-hand recommendation panel
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TurnResultDto {
    private TranscriptDto transcript;
    private ClaudeDecisionDto decision;
}
