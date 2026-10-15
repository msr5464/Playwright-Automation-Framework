package automation.core;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The element-fingerprinting script, shared byte-for-byte with the Python locator engine —
 * a resource rather than a string constant so the two cannot drift apart silently.
 * {@code test_capture_parity.py} asserts both stacks agree on a live page.
 *
 * <p>Never throws: a missing script costs the diagnosis confidence, not the run.
 */
public final class LocatorCapture {

    private static final String RESOURCE = "/locator-capture.js";

    /** Where a frame's element sits among its parent's frame elements, in document order. */
    private static final String ORDINAL =
            "el => [...el.ownerDocument.querySelectorAll('iframe, frame')].indexOf(el)";

    // ponytail: a fixed cap keeps ad-heavy pages from bloating the snapshot; raise it if a
    // real flow nests more frames than this.
    private static final int MAX_FRAMES = 20;

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
        return page == null ? null : snapshot(page.mainFrame());
    }

    /** One frame's document as fingerprints, or null if it could not be captured. */
    public static Object snapshot(Frame frame) {
        String js = script();
        if (frame == null || js.isEmpty())
            return null;
        try {
            return frame.evaluate(js);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Every iframe on the page, parents before children: its HTML, its element capture, and
     * which frame element of which parent it is. {@code page.content()} holds the top
     * document only, so without this an element inside an iframe looks absent to every
     * later reader of the failure. The page's own frame is index 0 and is not listed.
     */
    public static List<Map<String, Object>> frames(Page page) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (page == null)
            return out;
        try {
            List<Frame> all = new ArrayList<>(page.frames());
            all.sort(Comparator.comparingInt(LocatorCapture::depth));
            for (Frame frame : all) {
                if (frame.parentFrame() == null || out.size() >= MAX_FRAMES)
                    continue;
                try {
                    Map<String, Object> record = new LinkedHashMap<>();
                    record.put("index", all.indexOf(frame));
                    record.put("parent", all.indexOf(frame.parentFrame()));
                    record.put("ordinal", frame.frameElement().evaluate(ORDINAL));
                    record.put("url", frame.url());
                    record.put("html", frame.content());
                    record.put("fingerprints", snapshot(frame));
                    out.add(record);
                } catch (Throwable ignored) {
                    // A frame that detached mid-capture costs itself, never the others.
                }
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    private static int depth(Frame frame) {
        int depth = 0;
        for (Frame f = frame.parentFrame(); f != null; f = f.parentFrame())
            depth++;
        return depth;
    }
}
