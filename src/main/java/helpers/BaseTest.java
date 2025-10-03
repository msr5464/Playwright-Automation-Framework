package helpers;

import com.microsoft.playwright.Playwright;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.ITestResult;
import java.util.Date;

@Listeners(TestListener.class)
public class BaseTest {

    public static ThreadLocal<Config[]> threadLocalConfig = new ThreadLocal<Config[]>();
    public Config config;
    
    @BeforeSuite
    public void beforeSuite() {
        if (Config.playwright == null) {
            try {
                Config.playwright = Playwright.create();
                Config.playwright.selectors().setTestIdAttribute("data-cy");
                System.out.println("Playwright created successfully for test suite");
            } catch (Exception e) {
                System.err.println("Failed to create Playwright instance: " + e.getMessage());
                throw new RuntimeException("Failed to initialize Playwright", e);
            }
        }
    }

    @AfterSuite
    public void afterSuite() {
        try {
            // Clean up browser instance
            if (Config.browser != null) {
                Config.browser.close();
                Config.browser = null;
                System.out.println("Browser closed successfully for test suite");
            }
            
            // Clean up Playwright instance
            if (Config.playwright != null) {
                Config.playwright.close();
                Config.playwright = null;
                System.out.println("Playwright closed successfully for test suite");
            }
        } catch (Exception e) {
            // Log cleanup errors but don't fail the test
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    private Date testStartTime;
    private Date testEndTime;
    
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        config = new Config();
        config.testcaseName = result.getMethod().getMethodName();
        config.testcaseClass = result.getTestClass().getName();
        testStartTime = new Date();
        threadLocalConfig.set(new Config[] { config });
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (config != null) {
            BrowserHelper.closeBrowser(config);
        }

        // Set test end time
        testEndTime = new Date();

        // Calculate total execution time
        long totalTimeInMillis = testEndTime.getTime() - testStartTime.getTime();
        long totalTimeInSeconds = totalTimeInMillis / 1000;
        long minutes = totalTimeInSeconds / 60;
        long seconds = totalTimeInSeconds % 60;

        // Format time display
        String minuteOrMinutes = (minutes <= 1) ? "" : "s";
        String secondOrSeconds = (seconds <= 1) ? "" : "s";

        // Get test case name and class name
        String testcaseName = result.getMethod().getMethodName();
        String testcaseClass = result.getTestClass().getName();

        // Log test status and execution time
        if (result.getStatus() == ITestResult.SUCCESS) {
            config.logPass("<B>Passed test - '" + testcaseName + "' of Class '" + testcaseClass + "' took '" + minutes
                    + " minute" + minuteOrMinutes + " " + seconds + " second" + secondOrSeconds + "'</B>");
        } else if (result.getStatus() == ITestResult.FAILURE) {
            config.logFail("<B>Failed test '" + testcaseName + "' of Class '" + testcaseClass + "' took '" + minutes
                    + " minute" + minuteOrMinutes + " " + seconds + " second" + secondOrSeconds + "'</B>");
        } else if (result.getStatus() == ITestResult.SKIP) {
            config.logWarning("<B>Skipped test '" + testcaseName + "' of Class '" + testcaseClass + "' took '" + minutes
                    + " minute" + minuteOrMinutes + " " + seconds + " second" + secondOrSeconds + "'</B>");
        }
    }
}
