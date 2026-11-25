package data_access.gateway;

import java.util.TreeSet;

import entity.LlmPersona;
import use_case.edit_profile.LlmProfileEnrichmentGateway;

/** Produces an offline profile summary from the student's stated preferences. */
public final class RuleBasedProfileEnrichmentGateway implements LlmProfileEnrichmentGateway {

    @Override
    public LlmPersona enrichPersona(SanitizedProfileSnapshot snapshot) {
        final StringBuilder summary = new StringBuilder("Weekend preference: ")
                .append(snapshot.getWeekendStyleText());
        if (!snapshot.getBio().isBlank()) {
            summary.append(". Bio: ").append(snapshot.getBio());
        }
        if (!snapshot.getInterestedCommunityIds().isEmpty()) {
            summary.append(". Communities: ")
                    .append(String.join(", ", new TreeSet<>(snapshot.getInterestedCommunityIds())));
        }
        int end = Math.min(summary.length(), LlmPersona.MAX_SUMMARY_LENGTH);
        if (end < summary.length() && Character.isHighSurrogate(summary.charAt(end - 1))) {
            end--;
        }
        return LlmPersona.of(summary.substring(0, end));
    }
}
