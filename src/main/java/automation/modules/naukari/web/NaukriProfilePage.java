package automation.modules.naukari.web;

import automation.core.BasePage;
import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.WaitHelper;
import com.microsoft.playwright.Locator;

/**
 * Naukri profile page — exposes read and edit operations for the Profile Summary section.
 */
public class NaukriProfilePage extends BasePage
{
    private final Locator profileSummarySection     = page.locator("#profile-section-profile-summary");
    private final Locator profileSummaryDisplayText = page.locator("#profile-section-profile-summary div.rounded-2\\.5xl");
    private final Locator editProfileSummaryButton  = page.locator("#profile-section-profile-summary img[alt='PencilSimple']");
    private final Locator profileSummaryTextArea    = page.locator("textarea[placeholder='Craft a compelling profile summary']");
    private final Locator saveButton                = page.locator("div.bg-black button[type='submit']");
    private final Locator successToast              = page.locator("[class*='toast'], [class*='snackBar'], [class*='msgBlock']");

    public NaukriProfilePage(Config config)
    {
        super(config);
        assertPageLoaded(profileSummarySection);
    }

    /**
     * Returns the text currently displayed in the Profile Summary section.
     */
    public String getProfileSummaryText()
    {
        WaitHelper.waitForElementToBeVisible(config, profileSummaryDisplayText, "Profile Summary Display Text");
        return getText(profileSummaryDisplayText, "Profile Summary Text");
    }

    /**
     * Clicks the edit icon next to the Profile Summary section and waits for the
     * inline editor text area to become visible.
     */
    public void clickEditProfileSummary()
    {
        click(editProfileSummaryButton, "Edit Profile Summary button");
        WaitHelper.waitForElementToBeVisible(config, profileSummaryTextArea, "Profile Summary Text Area");
    }

    /**
     * Replaces the entire content of the Profile Summary editor with the given text.
     *
     * @param text the new profile summary text to enter
     */
    public void clearAndTypeProfileSummary(String text)
    {
        fillText(profileSummaryTextArea, text, "Profile Summary Text Area");
    }

    /**
     * Clicks the Save button to persist the edited Profile Summary.
     */
    public void saveProfileSummary()
    {
        click(saveButton, "Save button");
    }

    /**
     * Returns true if a success confirmation toast appears within 5 seconds of saving.
     * The optional wait ensures the test is not blocked if the toast is dismissed quickly.
     */
    public boolean isSuccessToastVisible()
    {
        return WaitHelper.waitForOptionalElementToBeVisible(config, successToast, "Success Toast");
    }

    /**
     * Reloads the page and returns the freshly rendered Profile Summary text,
     * confirming that the previously saved change actually persisted.
     */
    public String refreshAndGetProfileSummaryText()
    {
        BrowserHelper.navigateTo(config, config.getRunTimeProperty("naukari.profile.url"));
        WaitHelper.waitForElementToBeVisible(config, profileSummarySection, "Profile Summary Section");
        return getProfileSummaryText();
    }
}
