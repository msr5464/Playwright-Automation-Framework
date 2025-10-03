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
		if (config.browserContext == null) {
			setBrowserContext(config);
		}
		if (config.page == null) {
			config.page = config.browserContext.newPage();
			// Set timeouts using Playwright's modern approach
			Long objectWaitTime = Long.parseLong(config.getRunTimeProperty("ObjectWaitTime"));
			config.page.setDefaultTimeout(objectWaitTime * 1000); // Convert to milliseconds
			config.page.setDefaultNavigationTimeout(objectWaitTime * 3 * 1000);
		}

		config.logComment("Navigating to URL : " + url);
		try {
			config.page.navigate(url);
		} catch (Exception e) {
			config.logExceptionAndFail("Failed to navigate to URL: " + url, e);
		}
	}

	public static void setBrowser(Config config) {
		// Use direct access to static browser
		Browser currentBrowser = Config.browser;
		if (currentBrowser == null) {
			String browserName = config.getRunTimeProperty("browser").toLowerCase().trim();
			config.logCommentForDebugging("Set Browser '" + browserName + "' for execution...");

			BrowserName browser = null;
			try {
				browser = BrowserName.valueOf(browserName);
			} catch (IllegalArgumentException e) {
				config.logFail("Invalid Browser name is passed: " + browserName);
				return;
			}

			BrowserType browserType = null;
			LaunchOptions launchOptions = getLaunchOptions(config, browser);

			switch (browser) {
				case firefox:
					browserType = Config.playwright.firefox();
					break;

				case chromium:
					browserType = Config.playwright.chromium();
					break;

				case webkit:
					browserType = Config.playwright.webkit();
					break;

				default:
					config.logFail("Unsupported browser: " + browserName);
					return;
			}

			try {
				Browser newBrowser = browserType.launch(launchOptions);
				// Set the browser directly
				Config.browser = newBrowser;
			} catch (Exception e) {
				config.logExceptionAndFail("Failed to set '" + browserName + "' browser", e);
			}
		}
	}
	
	/**
	 * Get launch options for different browsers with modern configurations
	 */
	private static LaunchOptions getLaunchOptions(Config config, BrowserName browser) {
		LaunchOptions launchOptions = new LaunchOptions();
		
		// Check if headless mode is configured
		String headlessMode = config.getRunTimeProperty("headless");
		boolean isHeadless = headlessMode != null ? Boolean.parseBoolean(headlessMode) : false;
		launchOptions.setHeadless(isHeadless);
		
		// Add slow motion for debugging if configured
		String slowMo = config.getRunTimeProperty("slowMo");
		if (slowMo != null) {
			launchOptions.setSlowMo(Integer.parseInt(slowMo));
		}

		switch (browser) {
			case firefox:
				// Firefox-specific options
				launchOptions.setFirefoxUserPrefs(java.util.Map.of(
					"dom.webnotifications.enabled", false,
					"media.navigator.streams.fake", true
				));
				break;

			case chromium:
				// Chromium-specific options
				launchOptions.setArgs(java.util.Arrays.asList(
					"--disable-blink-features=AutomationControlled",
					"--disable-web-security",
					"--allow-running-insecure-content",
					"--disable-extensions",
					"--no-sandbox",
					"--disable-dev-shm-usage"
				));
				break;

			case webkit:
				// WebKit-specific options (minimal configuration)
				break;

			default:
				break;
		}
		
		return launchOptions;
	}

	private static void setBrowserContext(Config config) {
		String browserName = config.getRunTimeProperty("browser").toLowerCase().trim();
		config.logComment("Launching '" + browserName + "' browser...");

		try {
			setBrowser(config);
			Browser browser = Config.browser;
			if (browser != null) {
				config.browserContext = browser.newContext();
				config.logCommentForDebugging("Browser launched successfully");
			} else {
				config.logFail("Browser instance is null, cannot create context");
			}
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
		Browser browser = Config.browser;
		if (browser != null) {
			config.browserContext = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(
					Paths.get(config.testResourcesPath + "loginStorage" + File.separator + fileName)));
		} else {
			config.logFail("Browser instance is null, cannot load stored session");
		}
	}
}