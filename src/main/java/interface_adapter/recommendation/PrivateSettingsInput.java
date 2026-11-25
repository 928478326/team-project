package interface_adapter.recommendation;

/** Self-editable settings from the profile form. Verification is assigned separately. */
public final class PrivateSettingsInput {
    private final String utorid;
    private final boolean shareContactInfo;

    /**
     * Captures the entered identifier and contact visibility.
     * @param utorid optional self-reported identifier
     * @param shareContactInfo whether peers may see the contact handle
     */
    public PrivateSettingsInput(String utorid, boolean shareContactInfo) {
        this.utorid = utorid;
        this.shareContactInfo = shareContactInfo;
    }

    public String getUtorid() {
        return utorid;
    }

    public boolean isShareContactInfo() {
        return shareContactInfo;
    }
}
