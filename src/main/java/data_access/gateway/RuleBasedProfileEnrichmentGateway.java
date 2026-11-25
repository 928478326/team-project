package data_access.gateway;

import java.util.StringJoiner;
import java.util.TreeSet;

import entity.LlmPersona;
import use_case.edit_profile.LlmProfileEnrichmentGateway;

/** Produces an offline profile summary from the student's stated preferences. */
public final class RuleBasedProfileEnrichmentGateway implements LlmProfileEnrichmentGateway {

    @Override
    public LlmPersona enrichPersona(SanitizedProfileSnapshot snapshot) {
        final StringJoiner summary = new StringJoiner(". ");
        if (!snapshot.getWeekendStyleText().isEmpty()) {
            summary.add("Weekend preference: " + snapshot.getWeekendStyleText());
        }
        if (!snapshot.getInterestedCommunityIds().isEmpty()) {
            summary.add("Communities: " + String.join(", ", new TreeSet<>(snapshot.getInterestedCommunityIds())));
        }
        return LlmPersona.of(summary.toString());
    }
}
