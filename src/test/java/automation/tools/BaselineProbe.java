package automation.tools;

import automation.core.Baseline;
import automation.core.Config;
import automation.modules.saucedemo.web.LoginPage;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Exercises the record → promote / record → discard lifecycle directly: a failing
 * test must leave no fingerprint, or the breakage becomes the baseline.
 *
 * <p>Usage: {@code BaselineProbe <url> promote|discard}
 */
public class BaselineProbe {

    public static void main(String[] args) throws Exception {
        String url = args[0];
        boolean promote = "promote".equals(args[1]);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1280, 900));
            page.navigate(url);

            Config config = new Config();
            config.page = page;
            config.testcaseClass = "BaselineProbe";
            config.testcaseName = "probe" + (promote ? "Promote" : "Discard");

            Baseline.record(config, new LoginPage(config));
            if (promote) {
                Baseline.promote(config);
            } else {
                Baseline.discard(config);
            }
            browser.close();
        }

        Path directory = Paths.get(System.getenv("BASELINE_DIR"));
        try (Stream<Path> files = Files.list(directory)) {
            long promoted = files.filter(p -> p.toString().endsWith(".json")).count();
            System.out.println("promoted_files=" + promoted);
        }
    }
}
