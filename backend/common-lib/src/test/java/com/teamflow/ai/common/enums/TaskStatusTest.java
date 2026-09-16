package com.teamflow.ai.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TaskStatus transition rules")
class TaskStatusTest {

    @Nested
    @DisplayName("legal transitions")
    class LegalTransitions {

        @Test
        @DisplayName("a backlog item can be promoted to TODO")
        void backlogToTodo() {
            assertTrue(TaskStatus.BACKLOG.canTransitionTo(TaskStatus.TODO));
        }

        @Test
        @DisplayName("work in review can be completed")
        void reviewToDone() {
            assertTrue(TaskStatus.IN_REVIEW.canTransitionTo(TaskStatus.DONE));
        }

        @Test
        @DisplayName("review can be sent back for more work")
        void reviewToInProgress() {
            assertTrue(TaskStatus.IN_REVIEW.canTransitionTo(TaskStatus.IN_PROGRESS));
        }

        @Test
        @DisplayName("a completed task can be reopened to TODO")
        void doneToTodo() {
            assertTrue(TaskStatus.DONE.canTransitionTo(TaskStatus.TODO));
        }
    }

    @Nested
    @DisplayName("illegal transitions")
    class IllegalTransitions {

        @Test
        @DisplayName("work cannot jump from TODO straight to DONE, bypassing review")
        void todoCannotSkipToDone() {
            assertFalse(TaskStatus.TODO.canTransitionTo(TaskStatus.DONE));
        }

        @Test
        @DisplayName("a cancelled task cannot be marked done")
        void cancelledCannotComplete() {
            assertFalse(TaskStatus.CANCELLED.canTransitionTo(TaskStatus.DONE));
        }

        @Test
        @DisplayName("a completed task cannot be cancelled")
        void doneCannotCancel() {
            assertFalse(TaskStatus.DONE.canTransitionTo(TaskStatus.CANCELLED));
        }

        @ParameterizedTest
        @EnumSource(TaskStatus.class)
        @DisplayName("no status transitions to itself")
        void neverTransitionsToItself(TaskStatus status) {
            assertFalse(status.canTransitionTo(status));
        }
    }

    @Test
    @DisplayName("DONE and CANCELLED are the only terminal states")
    void terminalStates() {
        assertTrue(TaskStatus.DONE.isTerminal());
        assertTrue(TaskStatus.CANCELLED.isTerminal());
        assertFalse(TaskStatus.BLOCKED.isTerminal());
        assertFalse(TaskStatus.IN_REVIEW.isTerminal());
    }
}
