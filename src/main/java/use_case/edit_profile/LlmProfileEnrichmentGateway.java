package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import entity.LlmPersona;

/**
 * Output port (Gateway interface) allowing {@link EditProfileInteractor} to request LLM enrichment
 * of a user's hidden {@link LlmPersona} whenever their public profile changes.
 *
 * <p><b>Privacy-by-Design (PII Scrubbing)</b>: This boundary accepts ONLY a
 * {@link SanitizedProfileSnapshot} stripped of Tier 2 self-only fields ({@code utorid}),
 * {@code contactInfo}, {@code username}, and {@code passwordHash}.
 */
public interface LlmProfileEnrichmentGateway {

    /**
     * Synthesizes an updated hidden {@link LlmPersona} from a PII-scrubbed profile snapshot.
     *
     * @param snapshot sanitized public preference snapshot plus prior hidden persona
     * @return updated {@link LlmPersona} (never exposed to UI layers)
     * @throws IllegalStateException if the remote LLM service is unreachable or fails
     */
    LlmPersona enrichPersona(SanitizedProfileSnapshot snapshot);

    /**
     * Returns a no-op gateway implementation that preserves the user's existing {@link LlmPersona}.
     *
     * @return identity enrichment gateway
     */
    static LlmProfileEnrichmentGateway identity() {
        return SanitizedProfileSnapshot::getExistingPersona;
    }

    /**
     * Immutable PII-scrubbed snapshot passed across the external LLM gateway boundary.
     */
    final class SanitizedProfileSnapshot {

        private final String displayName;
        private final String bio;
        private final String weekendStyleText;
        private final Set<String> interestedCommunityIds;
        private final LlmPersona existingPersona;

        /**
         * Constructs a sanitized profile snapshot safe for external LLM consumption.
         *
         * @param displayName visible student nickname
         * @param bio public short bio
         * @param weekendStyleText free-text weekend preference description
         * @param interestedCommunityIds normalized interested community IDs
         * @param existingPersona previously synthesized hidden persona (non-null)
         */
        public SanitizedProfileSnapshot(
                String displayName,
                String bio,
                String weekendStyleText,
                Set<String> interestedCommunityIds,
                LlmPersona existingPersona) {
            this.displayName = emptyIfNull(displayName);
            this.bio = emptyIfNull(bio);
            this.weekendStyleText = emptyIfNull(weekendStyleText);
            this.interestedCommunityIds = immutableSetCopy(interestedCommunityIds);
            this.existingPersona =
                    Objects.requireNonNull(existingPersona, "existingPersona must not be null");
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getBio() {
            return bio;
        }

        public String getWeekendStyleText() {
            return weekendStyleText;
        }

        public Set<String> getInterestedCommunityIds() {
            return interestedCommunityIds;
        }

        public LlmPersona getExistingPersona() {
            return existingPersona;
        }

        private static String emptyIfNull(String value) {
            final String result;
            if (value == null) {
                result = "";
            }
            else {
                result = value;
            }
            return result;
        }

        private static Set<String> immutableSetCopy(Set<String> source) {
            final Set<String> copy;
            if (source == null) {
                copy = Collections.emptySet();
            }
            else {
                copy = Collections.unmodifiableSet(new LinkedHashSet<>(source));
            }
            return copy;
        }
    }
}
