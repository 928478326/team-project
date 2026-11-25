package use_case.edit_profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import entity.Community;
import entity.LlmPersona;
import entity.WeekendStyle;

/** Enriches the hidden persona using only predefined preference categories. */
public interface LlmProfileEnrichmentGateway {

    /**
     * Generates a hidden summary from approved categories.
     * @param snapshot selected weekend preset and community IDs
     * @return generated persona, or null to keep the previous local persona
     */
    LlmPersona enrichPersona(SanitizedProfileSnapshot snapshot);

    /**
     * Creates a gateway that leaves the stored summary untouched.
     * @return no-op enrichment gateway
     */
    static LlmProfileEnrichmentGateway identity() {
        return snapshot -> null;
    }

    /** Immutable allowlist projection. Custom styles and unknown community IDs stay local. */
    final class SanitizedProfileSnapshot {
        private final String weekendStyleText;
        private final Set<String> interestedCommunityIds;

        /**
         * Projects the selected preferences onto the application's built-in categories.
         * @param style student's saved weekend style
         * @param interests selected community IDs
         */
        public SanitizedProfileSnapshot(WeekendStyle style, Set<String> interests) {
            String preset = "";
            if (style != null && style.isPreset()) {
                preset = style.getDisplayName();
            }
            weekendStyleText = preset;
            final Set<String> selected = new LinkedHashSet<>();
            for (Community community : Community.defaultCommunities()) {
                if (interests != null && interests.contains(community.getCommunityId())) {
                    selected.add(community.getCommunityId());
                }
            }
            interestedCommunityIds = Collections.unmodifiableSet(selected);
        }

        public String getWeekendStyleText() {
            return weekendStyleText;
        }

        public Set<String> getInterestedCommunityIds() {
            return interestedCommunityIds;
        }

        public boolean isEmpty() {
            return weekendStyleText.isEmpty() && interestedCommunityIds.isEmpty();
        }
    }
}
