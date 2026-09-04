package automation.core;

import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * The element-fingerprinting script, shared byte-for-byte with the Python locator engine —
 * a resource rather than a string constant so the two cannot drift apart silently.
 * {@code test_capture_parity.py} asserts both stacks agree on a live page.
 *
 * <p>Never throws: a missing script costs the diagnosis confidence, not the run.
 */
public final class LocatorCapture {

    private static final String RESOURCE = "/locator-capture.js";

    /** Empty string means "unavailable" — callers skip fingerprinting rather than fail. */
    private static volatile String script;

    private LocatorCapture() {
    }

    public static String script() {
        String cached = script;
        if (cached != null)
            return cached;
        String loaded = "";
        try (InputStream in = LocatorCapture.class.getResourceAsStream(RESOURCE)) {
            if (in != null)
                loaded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
        return script = loaded;
    }

    /** The whole page as fingerprints, or null if it could not be captured. */
    public static Object snapshot(Page page) {
        String js = script();
        if (page == null || js.isEmpty())
            return null;
        try {
            return page.evaluate(js);
        } catch (Throwable e) {
            return null;
        }
    }
}
