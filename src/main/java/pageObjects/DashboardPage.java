package pageObjects;

import com.microsoft.playwright.Locator;

import helpers.AssertHelper;
import helpers.Config;
import helpers.WaitHelper;

public class DashboardPage {

	private Config config;
	private Locator userNameWidget;

	public DashboardPage(Config config) {
		this.config = config;
		this.userNameWidget = config.page.locator("img[class*='avatar']").first();
		WaitHelper.waitForPageLoad(config, userNameWidget);
	}

	public void verifyDashboardPage() {
		AssertHelper.assertElementVisible(config, userNameWidget, "User Name Widget on Dashboard Page");
	}
}