package pageObjects;

import com.microsoft.playwright.Locator;

import helpers.Config;
import helpers.Element;
import helpers.TestDataReader;
import helpers.WaitHelper;

public class LoginPage {

	private Config config;
	private Locator userNameTextBox;
	private Locator passwordTextBox;
	private Locator signMeInBtn;

	public enum ExpectedLandingPageAfterLogin {
		DashboardPage, OtpPage, LoginPage
	}

	public LoginPage(Config config) {
		this.config = config;
		this.userNameTextBox = config.page.locator("#login_field");
		this.passwordTextBox = config.page.locator("#password");
		this.signMeInBtn = config.page.locator("input[type='submit'][value='Sign in']");
		WaitHelper.waitForPageLoad(config, userNameTextBox);
	}

	public Object doLogin(int loginDetailsSheetRow, ExpectedLandingPageAfterLogin expectedLandingPage) {
		// Reading data from csv sheet and then getting logged in
		TestDataReader loginDetails = config.getCsvFile("LoginDetails");
		String username = loginDetails.getData(config, loginDetailsSheetRow, "Username");
		String password = loginDetails.getData(config, loginDetailsSheetRow, "Password");
		Element.enterData(config, userNameTextBox, username, "UserName");
		Element.enterData(config, passwordTextBox, password, "Password");

		switch (expectedLandingPage) {
			case LoginPage:
				return this;
			case OtpPage:
				Element.click(config, signMeInBtn, "Sign In Button");
				return new OtpPage(config);
			case DashboardPage:
				Element.click(config, signMeInBtn, "Sign In Button");
				WaitHelper.waitforseconds(config, 30);
				return new DashboardPage(config);
			default:
				return this;
		}
	}
}