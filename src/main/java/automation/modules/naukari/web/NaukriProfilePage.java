package automation.modules.naukari.web;

import automation.core.BasePage;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import com.microsoft.playwright.Locator;

/**
 * Naukri profile page — reads and edits the Profile Summary section.
 */
public class NaukriProfilePage extends BasePage
{
    private final Locator profileSummarySection     = page.locator("#profile-section-profile-summary");
    private final Locator profileSummaryDisplayText = page.locator("#profile-section-profile-summary div.text-title16R");
    private final Locator editProfileSummaryButton  = page.locator("#profile-section-profile-summary span.cursor-pointer");
    private final Locator profileSummaryTextArea    = page.locator("#profile-section-profile-summary textarea");
    private final Locator saveButton                = page.locator("#profile-section-profile-summary button[type='submit']");

    private final String profileUrl;

    public NaukriProfilePage(Config config)
    {
        super(config);
        this.profileUrl = config.getRunTimeProperty("naukari.profile.url");
        assertPageLoaded(profileSummarySection);
    }

    /**
     * Returns the text currently displayed in the Profile Summary section.
     */
    public String getProfileSummaryText()
    {
        return getText(profileSummaryDisplayText, "Profile Summary display text");
    }

    /**
     * Clicks the edit icon next to the Profile Summary section and waits for the textarea to appear.
     */
    public void clickEditProfileSummary()
    {
        click(editProfileSummaryButton, "Edit Profile Summary button");
        WaitHelper.waitForElementToBeVisible(config, profileSummaryTextArea, "Profile Summary text area");
    }

    /**
     * Clears the Profile Summary textarea and types the given text.
     *
     * @param summary the new summary text to enter
     */
    public void clearAndTypeProfileSummary(String summary)
    {
        fillText(profileSummaryTextArea, summary, "Profile Summary text area");
    }

    /**
     * Clicks the Save button and waits for the network to become idle,
     * ensuring the save request completes before the caller proceeds.
     */
    public void saveProfileSummary()
    {
        click(saveButton, "Save button");
        WaitHelper.waitForNetworkIdle(config);
    }

    /**
     * Navigates back to the Naukri profile page and returns the text displayed
     * in the Profile Summary section after the reload.
     *
     * @return the Profile Summary text as shown on the refreshed profile page
     */
    public String refreshAndGetProfileSummaryText()
    {
        BrowserHelper.navigateTo(config, profileUrl);
        WaitHelper.waitForElementToBeVisible(config, profileSummaryDisplayText, "Profile Summary display text");
        return getText(profileSummaryDisplayText, "Profile Summary display text");
    }
}
