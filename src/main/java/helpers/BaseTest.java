package helpers;

import com.microsoft.playwright.Playwright;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestResult;
import org.testng.ITestContext;
import java.util.Date;

public class BaseTest {

    public static ThreadLocal<Config[]> threadLocalConfig = new ThreadLocal<Config[]>();
    public Config config;

    @BeforeClass
    public void beforeClass() {
        if (Config.playwright == null) {
            Config.playwright = Playwright.create();
            System.out.println("Playwright created successfully");
        }
    }

    @AfterClass
    public void afterClass() {
        if (Config.browser != null) {
            Config.browser.close();
            Config.browser = null;
        }
        if (Config.playwright != null) {
            Config.playwright.close();
            Config.playwright = null;
        }
        System.out.println("Playwright closed successfully");
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
