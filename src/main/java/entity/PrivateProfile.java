package entity;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable Value Object representing the <b>self-only private &amp; security</b> portion of a
 * student's profile (Tier 2 visibility: visible and editable ONLY by the owning user; never shown
 * raw to other students and never transmitted to external LLM APIs).
 *
 * <p>Entering a format-valid UTORid sets {@link #hasUtorid()} to {@code true}, whereas
 * {@link #isUoftVerified()} is modeled separately and remains {@code false} until an explicit
 * trusted verification step calls {@link #markUoftVerified()}.
 */
public final class PrivateProfile {

    /** Minimum length of a valid UofT UTORid when provided. */
    public static final int MIN_UTORID_LENGTH = 3;

    /** Maximum length of a valid UofT UTORid when provided. */
    public static final int MAX_UTORID_LENGTH = 16;

    /** Placeholder displayed on peer cards when contact sharing is disabled by the user. */
    public static final String HIDDEN_CONTACT_PLACEHOLDER = "Hidden by privacy settings";

    private static final Pattern UTORID_PATTERN =
            Pattern.compile("^[a-z0-9]{" + MIN_UTORID_LENGTH + "," + MAX_UTORID_LENGTH + "}$");

    private static final PrivateProfile DEFAULT_UNVERIFIED = new PrivateProfile("", true, false);

    private final String utorid;
    private final boolean shareContactInfo;
    private final boolean uoftVerified;

    /**
     * Constructs an unverified {@code PrivateProfile} from user-entered settings.
     *
     * @param utorid optional UofT UTORid (blank if unset, otherwise
     *     {@value #MIN_UTORID_LENGTH}..{@value #MAX_UTORID_LENGTH} alphanumeric chars)
     * @param shareContactInfo whether {@link PublicProfile#getContactInfo()} may be shown on peer cards
     * @throws IllegalArgumentException if {@code utorid} is non-blank but violates UTORid format rules
     */
    public PrivateProfile(String utorid, boolean shareContactInfo) {
        this(utorid, shareContactInfo, false);
    }

    /**
     * Canonical constructor restoring or assembling all Tier 2 state explicitly.
     *
     * @param utorid optional UofT UTORid
     * @param shareContactInfo whether contact info may be shown on peer cards
     * @param uoftVerified whether ownership of {@code utorid} has been verified by a trusted step
     * @throws IllegalArgumentException if {@code utorid} is invalid or {@code uoftVerified} is true
     *     while {@code utorid} is empty
     */
    public PrivateProfile(String utorid, boolean shareContactInfo, boolean uoftVerified) {
        this.utorid = normalizeAndValidateUtorid(utorid);
        if (uoftVerified && this.utorid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot mark UofT student status verified without a UTORid.");
        }
        this.shareContactInfo = shareContactInfo;
        this.uoftVerified = uoftVerified;
    }

    /**
     * Returns the default private profile for newly created accounts (no UTORid, unverified,
     * contact sharing enabled).
     *
     * @return default unverified {@code PrivateProfile}
     */
    public static PrivateProfile defaultUnverified() {
        return DEFAULT_UNVERIFIED;
    }

    /**
     * Returns an updated {@code PrivateProfile} applying self-edited form inputs while preserving
     * {@link #isUoftVerified()} only if a non-empty UTORid remains unchanged.
     *
     * @param newUtorid updated UTORid input
     * @param newShareContactInfo updated contact-sharing preference
     * @return updated {@code PrivateProfile}
     */
    public PrivateProfile withUserInput(String newUtorid, boolean newShareContactInfo) {
        final String normalized = normalizeAndValidateUtorid(newUtorid);
        final boolean keepVerified = this.uoftVerified
                && !normalized.isEmpty()
                && normalized.equals(this.utorid);
        return new PrivateProfile(normalized, newShareContactInfo, keepVerified);
    }

    /**
     * Returns a copy marked as verified after a trusted UofT ownership check completes.
     *
     * @return verified {@code PrivateProfile}
     * @throws IllegalArgumentException if no UTORid is present
     */
    public PrivateProfile markUoftVerified() {
        return new PrivateProfile(this.utorid, this.shareContactInfo, true);
    }

    /**
     * Returns the user's private UTORid (intended strictly for self-only profile views and
     * persistence, never for peer cards or external LLM prompts).
     *
     * @return normalized lowercase UTORid, or empty string if not provided
     */
    public String getUtorid() {
        return utorid;
    }

    /**
     * Indicates whether the user has entered a format-valid UTORid on their profile.
     *
     * @return {@code true} if a non-empty UTORid is recorded
     */
    public boolean hasUtorid() {
        return !utorid.isEmpty();
    }

    /**
     * Indicates whether the user permits displaying their contact info on peer match cards.
     *
     * @return {@code true} if contact info may be shown to peers
     */
    public boolean isShareContactInfo() {
        return shareContactInfo;
    }

    /**
     * Indicates whether student status has been confirmed by a trusted verification step
     * (distinct from merely entering a UTORid).
     *
     * @return {@code true} only after trusted verification
     */
    public boolean isUoftVerified() {
        return uoftVerified;
    }

    /**
     * Projects a user's raw contact handle into its peer-visible representation according to
     * {@link #isShareContactInfo()}.
     *
     * @param rawContactInfo raw contact handle from {@link PublicProfile}
     * @return raw contact info when sharing is enabled, otherwise {@value #HIDDEN_CONTACT_PLACEHOLDER}
     */
    public String maskContactInfo(String rawContactInfo) {
        final String sanitized;
        if (rawContactInfo == null) {
            sanitized = "";
        }
        else {
            sanitized = rawContactInfo.trim();
        }
        final String masked;
        if (shareContactInfo) {
            masked = sanitized;
        }
        else {
            masked = HIDDEN_CONTACT_PLACEHOLDER;
        }
        return masked;
    }

    @Override
    public boolean equals(Object obj) {
        final boolean equal;
        if (this == obj) {
            equal = true;
        }
        else if (!(obj instanceof PrivateProfile)) {
            equal = false;
        }
        else {
            final PrivateProfile that = (PrivateProfile) obj;
            equal = shareContactInfo == that.shareContactInfo
                    && uoftVerified == that.uoftVerified
                    && utorid.equals(that.utorid);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(utorid, shareContactInfo, uoftVerified);
    }

    @Override
    public String toString() {
        return "PrivateProfile[hasUtorid=" + hasUtorid()
                + ", uoftVerified=" + uoftVerified
                + ", shareContactInfo=" + shareContactInfo + "]";
    }

    private static String normalizeAndValidateUtorid(String rawUtorid) {
        final String normalized;
        if (rawUtorid == null || rawUtorid.isBlank()) {
            normalized = "";
        }
        else {
            normalized = rawUtorid.trim().toLowerCase(Locale.ROOT);
            if (!UTORID_PATTERN.matcher(normalized).matches()) {
                throw new IllegalArgumentException(
                        "utorid must be "
                                + MIN_UTORID_LENGTH
                                + "-"
                                + MAX_UTORID_LENGTH
                                + " alphanumeric characters.");
            }
        }
        return normalized;
    }
}
