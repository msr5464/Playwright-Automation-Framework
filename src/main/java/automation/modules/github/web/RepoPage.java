package automation.modules.github.web;

import automation.core.Config;
import automation.core.BasePage;
import com.microsoft.playwright.Locator;

/**
 * Page object for a GitHub repository page.
 * Covers the repository landing view including the README section.
 */
public class RepoPage extends BasePage
{
    private final Locator readmeSection        = page.locator("[data-cy='readme-section']");
    private final Locator readmeImages         = page.locator("[data-cy='readme-images']");
    private final Locator readmeFctScreenshot  = page.locator("[data-cy='readme-fct-screenshot']");

    public RepoPage(Config config)
    {
        super(config);
        assertPageLoaded(readmeSection);
    }

    /**
     * Returns true when the FCT Test Coverage screenshot image is visible in the README.
     */
    public boolean isFctScreenshotVisible()
    {
        return isElementDisplayed(readmeFctScreenshot);
    }

    /**
     * Returns the alt text or caption associated with the FCT Test Coverage screenshot.
     */
    public String getFctScreenshotAltOrCaption()
    {
        return getText(readmeFctScreenshot, "FCT Screenshot");
    }
}
