package automation.tools;

import automation.core.BrowserHelper;
import automation.core.Config;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Drives {@link BrowserHelper#captureDomSnapshot} against one URL, so the failure-time
 * capture can be exercised without engineering a real test failure.
 *
 * <p>Usage: {@code SnapshotDump <url>}
 */
public class SnapshotDump {

    public static void main(String[] args) throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1280, 900));
            page.navigate(args[0]);

            Config config = new Config();
            config.page = page;
            config.testcaseName = "snapshotDumpProbe";
            BrowserHelper.captureDomSnapshot(config);

            System.out.println("dom=" + config.domSnapshotPath);
            browser.close();
        }
    }
}
