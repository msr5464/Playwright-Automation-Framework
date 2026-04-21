package automation.modules.github.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;

/**
 * Page object for a GitHub repository page.
 * Provides locators and actions for the README section and embedded images.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection;
    private final Locator readmeImage;

    public RepoPage(Config config)
    {
        super(config);
        readmeSection = page.locator("article.markdown-body");
        readmeImage   = page.locator("img[src*='testrailPage1']");
        assertPageLoaded(readmeSection);
    }

    /**
     * Check whether the README image (src contains 'testrailPage1') is visible on the page.
     *
     * @return true if the image is displayed, false otherwise
     */
    public boolean isReadmeImageVisible()
    {
        return isElementDisplayed(readmeImage);
    }

    /**
     * Return a locator for any README image whose alt text contains the given substring.
     *
     * @param altText substring to match against the image alt attribute
     * @return Locator scoped to the markdown-body article
     */
    public Locator getReadmeImageByAltTextContaining(String altText)
    {
        return page.locator("article.markdown-body img[alt*='" + altText + "']");
    }
}
