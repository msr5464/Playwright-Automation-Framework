package automation.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Dumps the shared capture script's output for one URL.
 *
 * <p>Exists so a test can diff this against the Python engine's output for the same page.
 * The two sides load identical bytes, but they drive different Playwright versions and so
 * different Chromium builds — identical source does not by itself prove identical output,
 * and every similarity score depends on the two agreeing.
 *
 * <p>Usage: {@code CaptureDump <url> <output.json>}
 */
public class CaptureDump {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: CaptureDump <url> <output.json>");
            System.exit(2);
        }
        String js;
        try (InputStream in = CaptureDump.class.getResourceAsStream("/locator-capture.js")) {
            if (in == null)
                throw new IllegalStateException("locator-capture.js missing from resources");
            js = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1280, 900));
            page.navigate(args[0]);
            Object snapshot = page.evaluate(js);
            Files.write(Paths.get(args[1]),
                    new ObjectMapper().writerWithDefaultPrettyPrinter()
                            .writeValueAsString(snapshot).getBytes(StandardCharsets.UTF_8));
            browser.close();
        }
        System.out.println("captured -> " + args[1]);
    }
}
