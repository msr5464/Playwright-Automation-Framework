
import org.testng.annotations.Test;

import helpers.BaseTest;
import helpers.BrowserHelper;
import pageObjects.DashboardPage;
import pageObjects.HomePage;
import pageObjects.LoginPage;
import pageObjects.LoginPage.ExpectedLandingPageAfterLogin;

public class TestLoginFlows extends BaseTest {

	@Test(description = "This testcase is verifying login not required to access 'github.com' if stored login session is used")
	public void verifyLoginOnGitHubUsingStoredLogin() {
		// Set the browser and context from the stored login session
		BrowserHelper.loadStoredSessionToAvoidRelogin(config, "GitHubLoginStorage.json");

		// Navigate to GitHub Dashboard Page without Login
		BrowserHelper.openBrowserAndNavigateToUrl(config, config.getRunTimeProperty("githubUrl"));

		// Verify the Dashboard Page to ensure the page is loaded
		DashboardPage dashboardPage = new DashboardPage(config);
		dashboardPage.verifyDashboardPage();
	}

	@Test(description = "This testcase is verifying successful login flow on 'github.com' website and storing the login session for future usecases")
	public void storeFirstTimeLoginOnGitHub() {
		// Row number of 'LoginDetails' sheet to get the username/password from login
		int loginDetailsSheetRowNumber = 1;

		// Launch Browser and Navigate to Home page of website
		BrowserHelper.openBrowserAndNavigateToUrl(config, config.getRunTimeProperty("githubUrl"));
		HomePage homePage = new HomePage(config);

		// Navigate to Login Page
		LoginPage loginPage = (LoginPage) homePage.getLoginPage();

		// Now Login and reach to Dashboard Page
		DashboardPage dashboardPage = (DashboardPage) loginPage.doLogin(loginDetailsSheetRowNumber,
				ExpectedLandingPageAfterLogin.DashboardPage);
		dashboardPage.verifyDashboardPage();

		// Store the login session in a json file for future usecases
		BrowserHelper.storeSessionToAvoidRelogin(config, "GitHubLoginStorage.json");
	}
}