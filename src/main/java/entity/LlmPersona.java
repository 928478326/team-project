package entity;

import java.util.Objects;

/**
 * Immutable Value Object representing the <b>system-only / LLM-maintained hidden persona</b>
 * expanding a student's buddy-matching profile beyond explicit form inputs
 * (Tier 3 visibility: strictly invisible to ALL users, including the user themselves).
 *
 * <p>Updated exclusively by internal profile-enrichment or LLM gateway workflows based on
 * behavioral signals (e.g., joined events, study habits, and anonymized peer feedback).
 */
public final class LlmPersona {

    /** Maximum character length for newly synthesized hidden persona summaries. */
    public static final int MAX_SUMMARY_LENGTH = 1000;

    private static final LlmPersona EMPTY = new LlmPersona("");

    private final String summary;

    private LlmPersona(String summary) {
        this.summary = summary;
    }

    /**
     * Returns the empty initial persona assigned before any LLM enrichment has occurred.
     *
     * @return empty {@code LlmPersona}
     */
    public static LlmPersona empty() {
        return EMPTY;
    }

    /**
     * Creates a validated {@code LlmPersona} from free-text LLM synthesis output.
     *
     * @param rawSummary synthesized hidden persona text (null/blank normalized to {@link #empty()},
     *     max {@value #MAX_SUMMARY_LENGTH} chars)
     * @return immutable {@code LlmPersona}
     * @throws IllegalArgumentException if {@code rawSummary} exceeds {@value #MAX_SUMMARY_LENGTH} chars
     */
    public static LlmPersona of(String rawSummary) {
        final LlmPersona result;
        if (rawSummary == null || rawSummary.isBlank()) {
            result = EMPTY;
        }
        else {
            final String trimmed = rawSummary.trim();
            if (trimmed.length() > MAX_SUMMARY_LENGTH) {
                throw new IllegalArgumentException(
                        "LlmPersona summary must not exceed " + MAX_SUMMARY_LENGTH + " characters.");
            }
            result = new LlmPersona(trimmed);
        }
        return result;
    }

    /**
     * Restores a persisted {@code LlmPersona} from storage without rejecting pre-existing lengths.
     *
     * @param storedSummary stored summary string (null/blank normalized to {@link #empty()})
     * @return immutable {@code LlmPersona}
     */
    public static LlmPersona fromStored(String storedSummary) {
        final LlmPersona result;
        if (storedSummary == null || storedSummary.isBlank()) {
            result = EMPTY;
        }
        else {
            result = new LlmPersona(storedSummary.trim());
        }
        return result;
    }

    /**
     * Returns the internal free-text persona summary for use exclusively by backend matching and
     * LLM gateways (never expose in UI DTOs or ViewModels).
     *
     * @return hidden persona text, or empty string if uninitialized
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Indicates whether no hidden persona summary has been synthesized yet.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return summary.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        final boolean equal;
        if (this == obj) {
            equal = true;
        }
        else if (!(obj instanceof LlmPersona)) {
            equal = false;
        }
        else {
            final LlmPersona that = (LlmPersona) obj;
            equal = summary.equals(that.summary);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary);
    }

    @Override
    public String toString() {
        return "LlmPersona[hidden, length=" + summary.length() + "]";
    }
}
