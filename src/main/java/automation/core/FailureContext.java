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

    /** The element an interaction is currently waiting on, per thread. */
    private static final ThreadLocal<Pending> PENDING = new ThreadLocal<>();

    /** What WaitHelper was waiting for when it gave up, held until something fails. */
    private static final class Pending {
        final Locator locator;
        final String elementName;
        final long elapsedMs;
        final long budgetMs;

        Pending(Locator locator, String elementName, long elapsedMs, long budgetMs) {
            this.locator = locator;
            this.elementName = elementName;
            this.elapsedMs = elapsedMs;
            this.budgetMs = budgetMs;
        }
    }

    /**
     * Remember the element a wait just gave up on, in case the action then fails.
     *
     * <p>Recorded rather than acted on: most waits that time out are followed by an
     * interaction that succeeds anyway, and writing a failure context for each of those
     * would fill the report directory with accounts of failures that never happened.
     */
    public static void waitingOn(Locator locator, String elementName,
                                 long elapsedMs, long budgetMs) {
        try {
            PENDING.set(new Pending(locator, elementName, elapsedMs, budgetMs));
        } catch (Throwable ignored) {
            // Never let bookkeeping about a failure become one.
        }
    }

    /**
     * Record an interaction that failed on an element, when one is pending.
     *
     * <p>Until this existed only {@code assertPageLoaded} produced a failure context, so
     * every other failure — the majority of them — left nothing measured behind. The
     * agent reading the run had to approximate the page object's coverage by evaluating
     * selectors against a saved snapshot, where {@code getByRole} and XPath cannot be
     * evaluated at all, and had no way to tell an element that is absent from one that is
     * present but covered.
     *
     * <p>Writes at most once per test: the first failure is the one that explains the
     * run, and the ones after it are its consequences.
     */
    public static void writeForInteraction(Config config) {
        if (config == null || config.page == null || config.failureContextPath != null) {
            return;
        }
        Pending pending = null;  // what the last wait gave up on, if anything
        try {
            pending = PENDING.get();
        } catch (Throwable ignored) {
            // fall through
        }
        if (pending == null) {
            return;
        }
        PENDING.remove();
        write(config, ownerOf(config, pending.locator),
              java.util.Collections.singletonList(pending.locator),
              pending.elapsedMs, pending.budgetMs, null, "ELEMENT_INTERACTION");
    }

    /**
     * The page object that declares this exact locator, compared by identity.
     *
     * <p>Page objects hold their locators in final fields, so the instance that failed is
     * the instance the owner declares — no name matching, and correct even when a test
     * holds several page objects at once.
     */
    private static Object ownerOf(Config config, Locator locator) {
        if (locator == null) {
            return null;
        }
        List<Object> candidates;
        synchronized (config.pageObjects) {
            candidates = new ArrayList<>(config.pageObjects);
        }
        // Most recently constructed first: the page being worked on right now.
        for (int i = candidates.size() - 1; i >= 0; i--) {
            Object candidate = candidates.get(i);
            for (Class<?> type = candidate.getClass();
                 type != null && type != Object.class && type != BasePage.class;
                 type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (!Locator.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        if (field.get(candidate) == locator) {
                            return candidate;
                        }
                    } catch (Throwable ignored) {
                        // An unreadable field simply is not the one.
                    }
                }
            }
        }
        return null;
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
        write(config, pageObject, anchors, elapsedMs, budgetMs, domChangedDuringWait,
              "PAGE_NOT_LOADED");
    }

    private static void write(Config config, Object pageObject, List<Locator> anchors,
                              long elapsedMs, long budgetMs, Boolean domChangedDuringWait,
                              String kind) {
        // One account per test, and the first one is the one worth having. A broken
        // locator makes the click do nothing, which leaves the flow on the previous page,
        // which makes the *next* page object fail to load — so the last failure in a run
        // describes a page the test never reached and the first describes why. Letting
        // the cascade overwrite the cause is how a stale locator ends up reported as a
        // page that was never loaded.
        if (config == null || config.page == null || config.failureContextPath != null) {
            return;
        }
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("schema", 1);
            root.put("test", config.testcaseClass + "." + config.testcaseName);
            root.put("failedAt", DataGenerator.getCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss"));
            root.put("failure", failureBlock(pageObject, anchors, elapsedMs, budgetMs, kind));
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
                                                    long elapsedMs, long budgetMs, String kind) {
        Map<String, Object> failure = new LinkedHashMap<>();
        failure.put("kind", kind);
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
