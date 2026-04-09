package automation.core;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ViewportSize;

import automation.core.Enums.ProjectName;
import automation.core.Enums.VideoMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BrowserHelper
{

    public static void initBrowser(Config config)
    {
        String browserName = config.getRunTimeProperty("browserName", "chromium");
        boolean headless = !"false".equalsIgnoreCase(config.getRunTimeProperty("headless", "true"));

        // Skip browser init for API-only tests
        if ("api".equalsIgnoreCase(browserName))
        {
            Log.comment(config, "API-only test - skipping browser initialization");
            return;
        }

        config.playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless);

        // Slow motion for debugging
        String slowMo = config.getRunTimeProperty("slowMo");
        if (slowMo != null && !slowMo.isEmpty())
        {
            launchOptions.setSlowMo(Double.parseDouble(slowMo));
        }

        config.browser = switch (browserName.toLowerCase())
        {
            case "firefox" -> config.playwright.firefox().launch(launchOptions);
            case "webkit" -> config.playwright.webkit().launch(launchOptions);
            default -> config.playwright.chromium().launch(launchOptions);
        };

        // Browser context with optional video recording
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setViewportSize(new ViewportSize(1920, 1080));

        configureVideoRecording(config, contextOptions);

        config.browserContext = config.browser.newContext(contextOptions);
        config.page = config.browserContext.newPage();

        // Set timeouts (reuse WaitHelper's centralised timeout calculation)
        int timeoutMs = WaitHelper.getTimeout(config);
        config.page.setDefaultTimeout(timeoutMs);
        config.page.setDefaultNavigationTimeout(timeoutMs * 3L);

        Log.comment(config, "Browser initialized: " + browserName + " (headless=" + headless + ")");
    }

    private static void configureVideoRecording(Config config, Browser.NewContextOptions contextOptions)
    {
        VideoMode videoMode = VideoMode.fromString(config.getRunTimeProperty("VideoMode"));
        if (videoMode == VideoMode.ON || videoMode == VideoMode.ON_FAILURE)
        {
            String videoDir = Config.resultsDirectory + File.separator + "videos";
            new File(videoDir).mkdirs();
            contextOptions.setRecordVideoDir(Paths.get(videoDir))
                .setRecordVideoSize(1280, 720);
        }
    }

    public static void takeScreenshot(Config config)
    {
        if (config.page == null) return;
        try
        {
            String screenshotDir = Config.resultsDirectory + File.separator + "screenshots";
            new File(screenshotDir).mkdirs();
            String fileName = config.testcaseName + "_" + DataGenerator.getCurrentDateTime("HHmmss") + ".png";
            Path screenshotPath = Paths.get(screenshotDir, fileName);
            config.page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(true));
            String link = "<a href='" + screenshotPath + "' target='_blank' style='color:#2563EB;'>&#128247; View Screenshot</a>";
            Log.comment(config, link);
            try {
                com.aventstack.chaintest.plugins.ChainTestListener.embed(Files.readAllBytes(screenshotPath), "image/png");
            } catch (Exception ignored) {}
        }
        catch (Exception e)
        {
            Log.warning(config, "Screenshot failed: " + e.getMessage());
        }
    }

    public static void closeBrowser(Config config)
    {
        try
        {
            // Capture video path before closing — page.video().path() is only valid while page is open
            VideoMode videoMode = VideoMode.fromString(config.getRunTimeProperty("VideoMode"));
            Path videoPath = null;
            if (videoMode != VideoMode.OFF && config.page != null && config.page.video() != null)
            {
                try { videoPath = config.page.video().path(); }
                catch (Exception e) { Log.debug(config, "Could not get video path: " + e.getMessage()); }
            }

            if (config.page != null)
            {
                config.page.close();
                config.page = null;
            }
            if (config.browserContext != null)
            {
                // Video file is finalized (written to disk) when the context closes
                config.browserContext.close();
                config.browserContext = null;
            }
            if (config.browser != null)
            {
                config.browser.close();
                config.browser = null;
            }
            if (config.playwright != null)
            {
                config.playwright.close();
                config.playwright = null;
            }

            // Log video link AFTER context is closed so the file is fully written
            handleVideoRetention(config, videoMode, videoPath);
        }
        catch (Exception e)
        {
            Log.error("Error closing browser: " + e.getMessage());
        }
    }

    private static void handleVideoRetention(Config config, VideoMode videoMode, Path videoPath)
    {
        if (videoMode == VideoMode.OFF || videoPath == null) return;
        try
        {
            if (videoMode == VideoMode.ON_FAILURE && config.testResult)
            {
                // Delete video for passing tests when mode is ON_FAILURE
                videoPath.toFile().delete();
                Log.debug(config, "Deleted video for passing test");
            }
            else
            {
                // Store path on config — TestBase.afterMethod logs it with the explicit
                // ITestResult so Reporter associates the link with the correct test entry.
                config.videoPath = videoPath.toString();
                System.out.println("[Video] Recorded: " + videoPath);
            }
        }
        catch (Exception e)
        {
            Log.warning(config, "Video handling error: " + e.getMessage());
        }
    }

    /**
     * Navigate to a URL
     */
    public static void navigateTo(Config config, String url)
    {
        if (config.page == null)
        {
            initBrowser(config);
        }
        Log.action(config, "Navigating to: " + url);
        config.page.navigate(url);
        WaitHelper.waitForPageLoad(config);
    }

    /**
     * Save the current browser session (cookies + localStorage) to a JSON file.
     * File is stored under src/test/resources/{moduleName}/loginStorage/.
     */
    public static void storeSession(Config config, ProjectName moduleName, String fileName)
    {
        try
        {
            String dir = Config.testResourcesPath + moduleName.name().toLowerCase() + "/loginStorage";
            new File(dir).mkdirs();
            Path filePath = Paths.get(dir, fileName);
            config.browserContext.storageState(
                new BrowserContext.StorageStateOptions().setPath(filePath));
            Log.comment(config, "Session stored: " + filePath);
        }
        catch (Exception e)
        {
            Log.warning(config, "Failed to store session: " + e.getMessage());
        }
    }

    /**
     * Initialize browser and load a previously stored session from a JSON file.
     * File is read from src/test/resources/{moduleName}/loginStorage/.
     */
    public static void initBrowserWithStoredSession(Config config, ProjectName moduleName, String fileName)
    {
        String browserName = config.getRunTimeProperty("browserName", "chromium");
        boolean headless = !"false".equalsIgnoreCase(config.getRunTimeProperty("headless", "true"));

        config.playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless);

        String slowMo = config.getRunTimeProperty("slowMo");
        if (slowMo != null && !slowMo.isEmpty())
        {
            launchOptions.setSlowMo(Double.parseDouble(slowMo));
        }

        config.browser = switch (browserName.toLowerCase())
        {
            case "firefox" -> config.playwright.firefox().launch(launchOptions);
            case "webkit"  -> config.playwright.webkit().launch(launchOptions);
            default        -> config.playwright.chromium().launch(launchOptions);
        };

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setViewportSize(new ViewportSize(1920, 1080));

        Path sessionPath = Paths.get(Config.testResourcesPath + moduleName.name().toLowerCase() + "/loginStorage", fileName);
        if (sessionPath.toFile().exists())
        {
            contextOptions.setStorageStatePath(sessionPath);
            Log.comment(config, "Loaded stored session: " + sessionPath);
        }
        else
        {
            Log.warning(config, "Session file not found, starting fresh: " + sessionPath);
        }

        configureVideoRecording(config, contextOptions);

        config.browserContext = config.browser.newContext(contextOptions);
        config.page = config.browserContext.newPage();

        int timeoutMs = WaitHelper.getTimeout(config);
        config.page.setDefaultTimeout(timeoutMs);
        config.page.setDefaultNavigationTimeout(timeoutMs * 3L);

        Log.comment(config, "Browser initialized with stored session: " + browserName);
    }
}
