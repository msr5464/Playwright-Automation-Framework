package automation.core;

import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * The element-fingerprinting script, shared byte-for-byte with the Python locator engine.
 *
 * <p>Two things read it: {@link Baseline}, which records what each locator matched while a
 * test was passing, and {@link BrowserHelper}, which records what the page contained at the
 * moment one failed. The engine scores the second against the first.
 *
 * <p>It lives in a resource rather than a string constant because the Python side loads the
 * identical file. Two implementations of the same DOM walk would be free to drift apart, and
 * when they do nothing breaks loudly — every similarity score is simply computed against a
 * different idea of the page. {@code test_capture_parity.py} asserts both halves of that:
 * the files are identical, and both stacks produce the same fingerprints for a live page.
 *
 * <p>Never throws. A missing or unusable script costs the diagnosis some confidence later;
 * it is not part of the run.
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
