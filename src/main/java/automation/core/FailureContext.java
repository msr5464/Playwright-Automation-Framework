package automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes a machine-readable account of why a test failed, next to the DOM snapshot.
 *
 * <p>An element that is not found looks identical however it went missing. The page may
 * still have been loading, a request may have failed, an earlier click may have silently
 * done nothing, an overlay may have covered it, the wait may have been two seconds short,
 * or the session may have expired and left the flow on a page it was never meant to
 * reach. The failure message can say none of that, so an agent reading it has one
 * hypothesis available — a stale locator — and reaches for it every time.
 *
 * <p>This class collects what distinguishes those cases, at the moment of failure, for
 * every test with no per-test work:
 *
 * <ul>
 *   <li><b>per-anchor match counts</b> — separates absent from the DOM (0) from present
 *       but never visible (N), which is the difference between a renamed element and a
 *       covered one;</li>
 *   <li><b>page identity</b> — url, title, body class, first heading;</li>
 *   <li><b>readiness</b> — readyState and aria-busy, so "still loading" is a fact;</li>
 *   <li><b>page-object self-coverage</b> — how many of the page object's own locators are
 *       present, read by reflection so every page object reports it without knowing;</li>
 *   <li>the flight recorder's HTTP errors, JS errors and navigation history.</li>
 * </ul>
 *
 * <p>The human-readable failure message is deliberately left untouched. Several consumers
 * key off its current wording, and the JSON is the new contract for machines. Nothing here
 * may throw: a failure while explaining a failure must never replace the original one.
 */
public class FailureContext {

    /** Elements to count before giving up on a page-load anchor. */
    private static final int MATCH_CAP = 25;

    public static final String MARKER = "QA-FAILURE-CONTEXT";

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private FailureContext() {
    }

    /**
     * Record why {@code pageObject} could not confirm it had loaded.
     *
     * @param anchors  the locators that were waited for, in the order they were tried
     * @param elapsedMs how long the wait actually took
     * @param budgetMs  how long it was allowed to take
     */
    public static void write(Config config, Object pageObject, List<Locator> anchors,
                             long elapsedMs, long budgetMs, Boolean domChangedDuringWait) {
        if (config == null || config.page == null) {
            return;
        }
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("schema", 1);
            root.put("test", config.testcaseClass + "." + config.testcaseName);
            root.put("failedAt", DataGenerator.getCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss"));
            root.put("failure", failureBlock(pageObject, anchors, elapsedMs, budgetMs));
            root.put("page", pageBlock(config.page));
            root.put("pageObjectCoverage", coverageBlock(pageObject));
            root.put("domVolatility", volatilityBlock(domChangedDuringWait));
            root.put("navigation", copyOf(config.navigationHistory));
            root.put("httpErrors", copyOf(config.httpErrors));
            root.put("jsErrors", copyOf(config.jsErrors));

            String json = MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(root);
            Path path = pathFor(config);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
            config.failureContextPath = path.toString();
            Log.debug(config, MARKER + " " + path);
        } catch (Throwable ignored) {
            // Explaining a failure must never become one.
        }
    }

    private static Path pathFor(Config config) {
        String dir = Config.resultsDirectory + File.separator + "dom";
        new File(dir).mkdirs();
        String name = config.testcaseName + "_"
                + DataGenerator.getCurrentDateTime("HHmmss") + ".context.json";
        return Paths.get(dir, name);
    }

    private static Map<String, Object> failureBlock(Object pageObject, List<Locator> anchors,
                                                    long elapsedMs, long budgetMs) {
        Map<String, Object> failure = new LinkedHashMap<>();
        failure.put("kind", "PAGE_NOT_LOADED");
        failure.put("pageObject", pageObject == null ? "" : pageObject.getClass().getSimpleName());

        List<Map<String, Object>> described = new ArrayList<>();
        for (Locator anchor : anchors == null ? new ArrayList<Locator>() : anchors) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("selector", String.valueOf(anchor));
            // The single most informative number available, and one call to get it:
            // zero means the element is not in the DOM at all, while any other
            // number means it is there and something stopped it being visible.
            entry.put("count", countOf(anchor));
            entry.put("visible", visibilityOf(anchor));
            described.add(entry);
        }
        failure.put("anchors", described);
        failure.put("elapsedMs", elapsedMs);
        failure.put("budgetMs", budgetMs);
        return failure;
    }

    private static Object countOf(Locator locator) {
        try {
            return Math.min(locator.count(), MATCH_CAP);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Object visibilityOf(Locator locator) {
        try {
            return locator.first().isVisible();
        } catch (Throwable e) {
            return null;
        }
    }

    private static Map<String, Object> pageBlock(Page page) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("url", safe(page::url));
        block.put("title", safe(page::title));
        block.put("bodyClass", safeEval(page, "document.body ? document.body.className : ''"));
        block.put("h1", safeEval(page, "(document.querySelector('h1')||{}).textContent || ''"));
        block.put("readyState", safeEval(page, "document.readyState"));
        block.put("ariaBusy", safeEval(page,
                "document.body ? document.body.getAttribute('aria-busy') : null"));
        return block;
    }

    /**
     * How many of the page object's own locators are present.
     *
     * <p>Read by reflection over the declared {@link Locator} fields, so every page object
     * in the repository reports this without a line of its own code. Zero of several is
     * the strongest available evidence that the flow is on the wrong page entirely, and
     * it needs no knowledge of the application to interpret.
     */
    private static Map<String, Object> coverageBlock(Object pageObject) {
        Map<String, Object> coverage = new LinkedHashMap<>();
        if (pageObject == null) {
            return coverage;
        }
        int matched = 0;
        int evaluable = 0;
        Map<String, Object> details = new LinkedHashMap<>();

        // Stop at BasePage. Its common locators — spinners, progress bars, error
        // banners — are absent on a healthy page by design, so counting them drags
        // every page object's ratio down by a fixed amount and blurs the one
        // distinction this measurement exists to make: a page object whose own
        // elements are all missing versus one with a single stale locator.
        for (Class<?> type = pageObject.getClass();
             type != null && type != Object.class && type != BasePage.class;
             type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!Locator.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Locator locator = (Locator) field.get(pageObject);
                    if (locator == null) {
                        continue;
                    }
                    int count = locator.count();
                    evaluable++;
                    if (count > 0) {
                        matched++;
                    }
                    details.put(field.getName(), count);
                } catch (Throwable ignored) {
                    // A locator we cannot evaluate is unknown, not absent. Leaving it
                    // out of `evaluable` is what keeps those two apart.
                }
            }
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("matched", matched);
        report.put("evaluable", evaluable);
        report.put("details", details);
        coverage.put(pageObject.getClass().getSimpleName(), report);
        return coverage;
    }

    private static Map<String, Object> volatilityBlock(Boolean changed) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("changedDuringWait", changed);
        return block;
    }

    private static List<String> copyOf(List<String> source) {
        synchronized (source) {
            return new ArrayList<>(source);
        }
    }

    private interface Supplier {
        String get();
    }

    private static String safe(Supplier supplier) {
        try {
            String value = supplier.get();
            return value == null ? "" : value;
        } catch (Throwable e) {
            return "";
        }
    }

    private static String safeEval(Page page, String expression) {
        try {
            Object value = page.evaluate("() => " + expression);
            return value == null ? "" : String.valueOf(value);
        } catch (Throwable e) {
            return "";
        }
    }
}
