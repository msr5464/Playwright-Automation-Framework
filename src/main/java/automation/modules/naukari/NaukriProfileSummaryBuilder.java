package automation.modules.naukari;

/**
 * Fluent builder for constructing NaukriProfileSummaryData test payloads.
 */
public class NaukriProfileSummaryBuilder
{
    private String profileSummary;

    public NaukriProfileSummaryBuilder withProfileSummary(String profileSummary)
    {
        this.profileSummary = profileSummary;
        return this;
    }

    /**
     * Set default values for optional fields that are still null.
     */
    public NaukriProfileSummaryBuilder withDefaults()
    {
        return this;
    }

    /**
     * Build the NaukriProfileSummaryData object.
     */
    public NaukriProfileSummaryData build()
    {
        withDefaults();
        NaukriProfileSummaryData data = new NaukriProfileSummaryData();
        data.setProfileSummary(profileSummary);
        return data;
    }
}
