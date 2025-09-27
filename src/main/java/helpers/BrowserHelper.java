package helpers;

import java.io.File;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;

public class BrowserHelper {
	private enum BrowserName {
		chromium, firefox, webkit
	};

	public static void openBrowserAndNavigateToUrl(Config config, String url) {
		if (config.browserContext == null)
			setBrowserContext(config);
		if (config.page == null)
			config.page = config.browserContext.newPage();

		config.logComment("Navigating to URL : " + url);
		config.page.navigate(url);
	}

	public static void setBrowser(Config config) {
		if (Config.browser == null) {
			String browserName = config.getRunTimeProperty("browser").toLowerCase().trim();
			config.logCommentForDebugging("Set Browser '" + browserName + "' for execution...");

			BrowserName browser = null;
			try {
				browser = BrowserName.valueOf(browserName);
			} catch (IllegalArgumentException e) {
				config.logFail("Invalid Browser name is passed");
				return;
			}

			BrowserType browserType = null;
			LaunchOptions launchOptions = new LaunchOptions();

			switch (browser) {
				case firefox:
					browserType = Config.playwright.firefox();
					launchOptions.setHeadless(false);
					break;

				case chromium:
					browserType = Config.playwright.chromium();
					launchOptions.setHeadless(false);
					launchOptions.setArgs(java.util.Arrays.asList(
							"--disable-infobars",
							"--start-fullscreen",
							"--disable-blink-features=AutomationControlled",
							"--disable-extensions",
							"--no-sandbox",
							"--disable-dev-shm-usage"));
					break;

				case webkit:
					browserType = Config.playwright.webkit();
					launchOptions.setHeadless(false);
					break;

				default:
					config.logFail("Unsupported browser: " + browserName);
					return;
			}

			try {
				Config.browser = browserType.launch(launchOptions);
			} catch (Exception e) {
				config.logExceptionAndFail("Failed to set '" + browserName + "' browser", e);
			}
		}
	}

	private static void setBrowserContext(Config config) {

		String browserName = config.getRunTimeProperty("browser").toLowerCase().trim();
		config.logComment("Launching '" + browserName + "' browser...");

		try {
			setBrowser(config);
			config.browserContext = Config.browser.newContext();

			// Set timeouts
			Long ObjectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
			config.page.setDefaultTimeout(ObjectWaitTime * 1000); // Convert to milliseconds
			config.page.setDefaultNavigationTimeout(ObjectWaitTime * 3 * 1000);

			config.logCommentForDebugging("Browser launched successfully");
		} catch (Exception e) {
			config.logExceptionAndFail("Failed to launch browser", e);
		}
	}

	public static void takeScreenshot(Config config) {
		if (config.page == null) {
			config.logComment("Page is NULL, so can't take screenshot!");
		} else {
			File screenshotUrl = getScreenShotFile(config);
			try {
				config.page
						.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotUrl.getAbsolutePath())));
				config.logComment("Screenshot saved to: " + screenshotUrl.getAbsolutePath());
			} catch (Exception e) {
				config.logWarning("Unable to take screenshot: " + e.getMessage());
				e.printStackTrace();
			}

			String href = convertFilePathToHtmlUrl(screenshotUrl.getPath());
			config.logComment(
					"<B>Screenshot</B>:- <a href=" + href + " target='_blank' >" + screenshotUrl.getName() + "</a>");
			config.logComment("<B>Page URL</B>:- <a href=" + config.page.url()
					+ " target='_blank' >" + config.page.url() + "</a>");
		}
	}

	public static File getScreenShotFile(Config config) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String screenshotName = config.testcaseName + "_" + dateFormat.format(date) + ".png";
		File resultsDir = new File(config.getRunTimeProperty("ResultsDirectory"));
		if (!resultsDir.exists()) {
			resultsDir.mkdirs();
		}
		File dest = new File(resultsDir.getPath() + File.separator + screenshotName);
		return dest;
	}

	// Helper method to convert file path to HTML URL (replacing CommonUtilities
	// dependency)
	private static String convertFilePathToHtmlUrl(String filePath) {
		return "file://" + filePath.replace("\\", "/");
	}

	// Method to close browser and clean up resources
	public static void closeBrowser(Config config) {
		try {
			if (config.page != null) {
				config.page.close();
				config.page = null;
			}
			if (config.browserContext != null) {
				config.browserContext.close();
				config.browserContext = null;
			}
			config.logCommentForDebugging("Browser closed successfully");
		} catch (Exception e) {
			config.logWarning("Error while closing browser: " + e.getMessage());
		}
	}

	public static void storeSessionToAvoidRelogin(Config config, String fileName) {
		config.browserContext.storageState(new BrowserContext.StorageStateOptions().setPath(
				Paths.get(config.testResourcesPath + "loginStorage" + File.separator + fileName)));
	}

	public static void loadStoredSessionToAvoidRelogin(Config config, String fileName) {
		setBrowser(config);
		config.browserContext = Config.browser.newContext(new Browser.NewContextOptions().setStorageStatePath(
				Paths.get(config.testResourcesPath + "loginStorage" + File.separator + fileName)));
	}
}