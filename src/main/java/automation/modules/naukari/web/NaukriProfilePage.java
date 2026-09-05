package automation.modules.naukari.web;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import automation.core.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for the Naukri user profile page.
 * Covers interactions with the Profile Summary section only.
 */
public class NaukriProfilePage extends BasePage
{
    private final String  profileUrl               = config.getRunTimeProperty("naukari.profile.url");
    private final Locator profileSummarySection     = page.locator("#profile-section-profile-summary");
    private final Locator profileSummaryDisplayText = page.locator("#profile-section-profile-summary div.text-title16R");
    private final Locator editProfileSummaryButton  = page.locator("#profile-section-profile-summary span.cursor-pointer");
    private final Locator profileSummaryTextArea    = page.locator("#profile-section-profile-summary textarea");
    private final Locator saveButton                = page.locator("#profile-section-profile-summary button[type='submit']");

    public NaukriProfilePage(Config config)
    {
        super(config);
        assertPageLoaded(profileSummarySection);
    }

    /**
     * Return the text currently displayed in the Profile Summary section.
     */
    public String getProfileSummaryText()
    {
        return getText(profileSummaryDisplayText, "Profile Summary text");
    }

    /**
     * Click the edit icon next to the Profile Summary section and wait for the
     * text area to become visible before returning.
     */
    public void clickEditProfileSummary()
    {
        click(editProfileSummaryButton, "Edit Profile Summary button");
        WaitHelper.waitForElementToBeVisible(config, profileSummaryTextArea, "Profile Summary text area");
    }

    /**
     * Clear the Profile Summary text area and fill it with the provided text.
     *
     * @param text new profile summary content
     */
    public void clearAndTypeProfileSummary(String text)
    {
        fillText(profileSummaryTextArea, text, "Profile Summary text area");
    }

    /**
     * Click the Save button and wait for the network activity to settle,
     * confirming the change was persisted.
     */
    public void saveProfileSummary()
    {
        click(saveButton, "Save button");
        WaitHelper.waitForNetworkIdle(config);
    }

    /**
     * Navigate back to the profile page and return the Profile Summary text
     * as displayed after the page has fully loaded.
     *
     * @return the Profile Summary displayed text after reload
     */
    public String refreshAndGetProfileSummaryText()
    {
        BrowserHelper.navigateTo(config, profileUrl);
        WaitHelper.waitForElementToBeVisible(config, profileSummaryDisplayText, "Profile Summary text");
        return getText(profileSummaryDisplayText, "Profile Summary text");
    }
}
