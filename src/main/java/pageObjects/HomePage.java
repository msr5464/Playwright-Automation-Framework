package pageObjects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import helpers.Config;
import helpers.Element;
import helpers.WaitHelper;

public class HomePage {

	private Config config;
	private Locator signInButton;

	public HomePage(Config config) {
		this.config = config;
		this.signInButton = config.page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in"));
		WaitHelper.waitForPageLoad(config, signInButton);
	}

	public LoginPage getLoginPage() {
		Element.click(config, signInButton, "Sign In Button");
		return new LoginPage(config);
	}
}