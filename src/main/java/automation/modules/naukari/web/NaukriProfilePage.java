package automation.modules.naukari.web;

import com.microsoft.playwright.Locator;
import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class NaukriProfilePage extends BasePage
{
    private final Locator profileSummaryText;
    private final Locator editProfileSummaryButton;
    private final Locator summaryTextArea;
    private final Locator saveButton;

    public NaukriProfilePage(Config config)
    {
        super(config);
        profileSummaryText       = page.locator("#profile-section-profile-summary .whitespace-pre-line");
        editProfileSummaryButton = page.locator("#profile-section-profile-summary span.cursor-pointer");
        summaryTextArea          = page.locator("textarea[placeholder='Craft a compelling profile summary']");
        saveButton               = page.locator("#profile-section-profile-summary button[type='submit']");
        assertPageLoaded(profileSummaryText);
    }

    public String getCurrentSummaryText()
    {
        Log.comment(config, "Reading current profile summary text");
        return getText(profileSummaryText, "Profile summary text");
    }

    public void clickEditSummary()
    {
        Log.comment(config, "Clicking edit button for Profile Summary section");
        click(editProfileSummaryButton, "Edit Profile Summary button");
    }

    public void setSummaryText(String summaryText)
    {
        Log.comment(config, "Entering updated profile summary text");
        fillText(summaryTextArea, summaryText, "Summary text area");
    }

    public void clickSave()
    {
        Log.comment(config, "Clicking Save button for Profile Summary");
        click(saveButton, "Save button");
        WaitHelper.waitForNetworkIdle(config);
    }

    public String getDisplayedSummaryText()
    {
        Log.comment(config, "Reading displayed profile summary text");
        return getText(profileSummaryText, "Profile summary text");
    }
}
