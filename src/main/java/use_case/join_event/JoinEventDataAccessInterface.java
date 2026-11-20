package use_case.join_event;

import entity.Event;
import java.time.Instant;
import java.util.Optional;

/**
 * Narrow Data Access Interface for joining an event atomically (ISP).
 * Implementations (both SQLite and InMemory) MUST honor the exact atomic capacity
 * and idempotency contract defined by {@link #joinAtomically(String, String, Instant)} (LSP).
 */
public interface JoinEventDataAccessInterface {

    /**
     * Finds an event by ID.
     *
     * @param eventId target event ID
     * @return event if found
     */
    Optional<Event> findEventById(String eventId);

    /**
     * Atomically checks existing participation, open-window, and capacity, and inserts
     * a participation row if allowed.
     *
     * @param userId joining user ID
     * @param eventId target event ID
     * @param now current UTC instant from injected {@link java.time.Clock}
     * @return atomic outcome including latest count and original {@code joinedAt} if already joined
     */
    JoinAttemptResult joinAtomically(String userId, String eventId, Instant now);

    /**
     * Enumerates all deterministic outcomes of {@link #joinAtomically(String, String, Instant)}.
     */
    enum Outcome {
        JOINED,
        ALREADY_JOINED,
        FULL,
        EVENT_CLOSED,
        EVENT_NOT_FOUND
    }

    /**
     * Immutable result object returned by {@link #joinAtomically(String, String, Instant)}.
     */
    final class JoinAttemptResult {

        private final Outcome outcome;
        private final int joinedCount;
        private final int capacity;
        private final Instant joinedAt;

        public JoinAttemptResult(Outcome outcome, int joinedCount, int capacity, Instant joinedAt) {
            this.outcome = outcome;
            this.joinedCount = joinedCount;
            this.capacity = capacity;
            this.joinedAt = joinedAt;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public int getJoinedCount() {
            return joinedCount;
        }

        public int getCapacity() {
            return capacity;
        }

        public Instant getJoinedAt() {
            return joinedAt;
        }
    }
}
