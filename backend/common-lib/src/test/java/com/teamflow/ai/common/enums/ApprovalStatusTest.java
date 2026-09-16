package com.teamflow.ai.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ApprovalStatus lifecycle rules")
class ApprovalStatusTest {

    @Test
    @DisplayName("PENDING and UNDER_REVIEW are decidable")
    void openStatusesAreDecidable() {
        assertTrue(ApprovalStatus.PENDING.isDecidable());
        assertTrue(ApprovalStatus.UNDER_REVIEW.isDecidable());
    }

    @Test
    @DisplayName("APPROVED, REJECTED and CANCELLED are terminal and not decidable again")
    void terminalStatusesAreNotDecidable() {
        assertTrue(ApprovalStatus.APPROVED.isTerminal());
        assertTrue(ApprovalStatus.REJECTED.isTerminal());
        assertTrue(ApprovalStatus.CANCELLED.isTerminal());
        assertFalse(ApprovalStatus.APPROVED.isDecidable());
        assertFalse(ApprovalStatus.REJECTED.isDecidable());
        assertFalse(ApprovalStatus.CANCELLED.isDecidable());
    }

    @Test
    @DisplayName("RETURNED_FOR_CHANGES is not terminal, but also not directly decidable again — "
            + "resubmission opens a new ApprovalRequest rather than re-deciding this one")
    void returnedForChangesIsADeadEndForDecideButNotTerminal() {
        assertFalse(ApprovalStatus.RETURNED_FOR_CHANGES.isTerminal());
        assertFalse(ApprovalStatus.RETURNED_FOR_CHANGES.isDecidable());
    }

    @ParameterizedTest
    @EnumSource(ApprovalStatus.class)
    @DisplayName("terminal and decidable are mutually exclusive for every status")
    void terminalAndDecidableAreMutuallyExclusive(ApprovalStatus status) {
        assertFalse(status.isTerminal() && status.isDecidable());
    }
}
