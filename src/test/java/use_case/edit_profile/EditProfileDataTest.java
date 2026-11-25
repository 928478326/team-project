package use_case.edit_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Unit tests verifying null-safety and immutability contracts of {@link EditProfileInputData},
 * {@link EditProfileOutputData}, and {@link LlmProfileEnrichmentGateway.SanitizedProfileSnapshot}.
 */
class EditProfileDataTest {

    @Test
    void testDtoAndSnapshotNullSafetyDefaults() {
        final EditProfileInputData input = new EditProfileInputData(
                null, EditProfileInputData.Action.SAVE, null, null, null, null, null)
                .withPrivateSettings(null, true);
        assertEquals("", input.getUserId());
        assertEquals(EditProfileInputData.Action.SAVE, input.getAction());
        assertEquals("", input.getDisplayName());
        assertEquals("", input.getBio());
        assertEquals("", input.getContactInfo());
        assertEquals("", input.getWeekendStyleCode());
        assertTrue(input.getInterestedCommunityIds().isEmpty());
        assertEquals("", input.getUtorid());
        assertTrue(input.isShareContactInfo());
        assertTrue(input.isPrivateProfileIncluded());

        final EditProfileOutputData legacyOutput = new EditProfileOutputData(
                "u-1", "Henry", null, null, "INDOOR_CHILL", null, null);
        assertEquals("", legacyOutput.getBio());
        assertEquals("", legacyOutput.getContactInfo());
        assertTrue(legacyOutput.getInterestedCommunityIds().isEmpty());
        assertEquals("", legacyOutput.getUtorid());
        assertFalse(legacyOutput.hasUtorid());
        assertTrue(legacyOutput.isShareContactInfo());
        assertFalse(legacyOutput.isUoftVerified());
        assertEquals("", legacyOutput.getStatusMessage());
        assertEquals("", legacyOutput.withPrivateStatus(null, false, false, false).getUtorid());

        final LlmProfileEnrichmentGateway.SanitizedProfileSnapshot snapshot =
                new LlmProfileEnrichmentGateway.SanitizedProfileSnapshot(null, null);
        assertEquals("", snapshot.getWeekendStyleText());
        assertTrue(snapshot.getInterestedCommunityIds().isEmpty());
        assertTrue(snapshot.isEmpty());
        assertNull(LlmProfileEnrichmentGateway.identity().enrichPersona(snapshot));
    }
}
