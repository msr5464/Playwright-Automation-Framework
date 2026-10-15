package automation.modules.naukari;

import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.naukari.web.NaukriProfilePage;

public class NaukriProfileSummaryHelper extends ApiHelper
{
    public NaukriProfileSummaryHelper(Config config)
    {
        super(config, config.getRunTimeProperty("naukari.url"));
    }

    /**
     * Toggle the trailing dot in the profile summary and save the change.
     *
     * @param profile      the already-loaded NaukriProfilePage
     * @param currentSummary the summary text currently displayed
     * @return the expected (saved) summary text after the toggle
     */
    public String toggleTrailingDotAndSave(NaukriProfilePage profile, String currentSummary)
    {
        String expectedSummary = currentSummary.endsWith(".")
            ? currentSummary.substring(0, currentSummary.length() - 1)
            : currentSummary + ".";

        Log.comment(config, "Toggling trailing dot — new expected summary length: " + expectedSummary.length());
        profile.clickEditSummary();
        profile.setSummaryText(expectedSummary);
        profile.clickSave();
        return expectedSummary;
    }
}
