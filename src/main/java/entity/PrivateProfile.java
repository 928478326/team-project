package entity;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable Value Object representing the <b>self-only private &amp; security</b> portion of a
 * student's profile (Tier 2 visibility: visible and editable ONLY by the owning user; never shown
 * raw to other students and never transmitted to external LLM APIs).
 *
 * <p>Peer-facing projections may expose only the derived {@link #isUoftVerified()} status badge
 * and {@link #maskContactInfo(String)} result.
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

    private static final PrivateProfile DEFAULT_UNVERIFIED = new PrivateProfile("", true);

    private final String utorid;
    private final boolean shareContactInfo;

    /**
     * Constructs an immutable {@code PrivateProfile}.
     *
     * @param utorid optional UofT UTORid for student verification (blank if unverified, otherwise
     *     {@value #MIN_UTORID_LENGTH}..{@value #MAX_UTORID_LENGTH} lowercase alphanumeric chars)
     * @param shareContactInfo whether {@link PublicProfile#getContactInfo()} may be shown on peer cards
     * @throws IllegalArgumentException if {@code utorid} is non-blank but violates UTORid format rules
     */
    public PrivateProfile(String utorid, boolean shareContactInfo) {
        this.utorid = normalizeAndValidateUtorid(utorid);
        this.shareContactInfo = shareContactInfo;
    }

    /**
     * Returns the default private profile for newly created accounts (unverified UTORid,
     * contact sharing enabled).
     *
     * @return default unverified {@code PrivateProfile}
     */
    public static PrivateProfile defaultUnverified() {
        return DEFAULT_UNVERIFIED;
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
     * Indicates whether the user permits displaying their contact info on peer match cards.
     *
     * @return {@code true} if contact info may be shown to peers
     */
    public boolean isShareContactInfo() {
        return shareContactInfo;
    }

    /**
     * Derived public-safe verification badge indicating whether a valid UofT UTORid is on file
     * without revealing the UTORid itself.
     *
     * @return {@code true} if verified UofT student
     */
    public boolean isUoftVerified() {
        return !utorid.isEmpty();
    }

    /**
     * Projects a user's raw contact handle into its peer-visible representation according to
     * {@link #isShareContactInfo()}.
     *
     * @param rawContactInfo raw contact handle from {@link PublicProfile}
     * @return raw contact info when sharing is enabled, otherwise {@value #HIDDEN_CONTACT_PLACEHOLDER}
     */
    public String maskContactInfo(String rawContactInfo) {
        final String sanitized = rawContactInfo == null ? "" : rawContactInfo.trim();
        return shareContactInfo ? sanitized : HIDDEN_CONTACT_PLACEHOLDER;
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
                    && utorid.equals(that.utorid);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(utorid, shareContactInfo);
    }

    @Override
    public String toString() {
        return "PrivateProfile[uoftVerified=" + isUoftVerified()
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
