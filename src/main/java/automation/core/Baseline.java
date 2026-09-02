package automation.core;

import com.microsoft.playwright.Locator;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record what a page looks like when a test reaches it successfully.
 *
 * <p>Deciding whether a failing page is "the right page with a renamed element" or "the
 * wrong page entirely" is guesswork without something to compare against. Coverage ratios
 * get most of the way there, but they answer relatively — this page object matches less of
 * itself than that one does — and a page object with one generic selector can score well
 * on almost anything.
 *
 * <p>A baseline makes it a diff. When the page object loaded successfully, its identity and
 * its own locator counts are written down; at failure the two are compared and the answer
 * stops depending on thresholds. It needs no knowledge of the application, and it gets
 * more useful the longer the suite runs.
 *
 * <p>Recorded when the page object confirms it has loaded, but held aside until the test
 * that recorded it passes. A page-load anchor being visible is not the test succeeding:
 * a page object whose identity anchor is present while a second locator is broken loads
 * perfectly well and fails a moment later. Writing the fingerprint straight out meant the
 * failing run — and the diagnosis probe that re-ran it minutes afterwards — overwrote the
 * record of the last good run with the broken page, under the name of the good one. Every
 * comparison against it then confirmed the breakage instead of contradicting it.
 *
 * <p>So {@link #record} writes to {@code baselines/pending/}, {@link #promote} moves it
 * into place when the test passes, and {@link #discard} throws it away when it does not.
 * Never allowed to fail a test: a baseline is an optimisation for a later diagnosis, not
 * part of the run.
 */
public class Baseline {

    /** Overridable so CI can point at a path that survives between builds. */
    private static final String DIR = System.getenv("BASELINE_DIR");

    private static final Set<String> WRITTEN = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Fingerprints cost one DOM walk per page object. Opt-out for a pathological page or
     * a debugging run; the coverage counts are recorded either way.
     */
    private static final boolean FINGERPRINTS =
            !"false".equalsIgnoreCase(String.valueOf(System.getenv("BASELINE_FINGERPRINTS")));



    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private Baseline() {
    }

    /** Fingerprint {@code pageObject} now that it has confirmed it loaded. */
    public static void record(Config config, Object pageObject) {
        if (config == null || config.page == null || pageObject == null)
            return;
        String name = pageObject.getClass().getSimpleName();
        // Once per test per page object. Keyed by test rather than by page alone: a
        // pending fingerprint is only promoted if *this* test passes, so a page first
        // seen by a test that fails must still be recordable by the next one.
        if (!WRITTEN.add(testKey(config) + "#" + name))
            return;

        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("pageObject", name);
            root.put("recordedAt", DataGenerator.getCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss"));
            root.put("urlShape", shapeOf(config.page.url()));
            root.put("title", config.page.title());
            root.put("bodyClass", bodyClass(config));
            Map<String, Object> counts = new LinkedHashMap<>();
            Map<String, Object> prints = new LinkedHashMap<>();
            List<Object> landmarks = new java.util.ArrayList<>();
            collect(config, pageObject, counts, prints, landmarks);
            root.put("coverage", counts);
            root.put("fingerprints", prints);
            // Headings and landmark roles. Cheap, and a far better answer to
            // "are we even on the right screen" than a URL, which survives a
            // redirect to a login page unchanged.
            root.put("landmarks", landmarks);

            Path directory = pendingDirectory();
            new File(directory.toString()).mkdirs();
            Files.write(directory.resolve(pendingName(config, name)),
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                            .getBytes(StandardCharsets.UTF_8));
        } catch (Throwable ignored) {
            // A missing baseline only lowers confidence later. It is never fatal.
        }
    }

    /** The test passed: everything it recorded really is a good-run fingerprint. */
    public static void promote(Config config) {
        forEachPending(config, (pending, pageObject) -> {
            Path directory = baselineDirectory();
            new File(directory.toString()).mkdirs();
            Files.move(pending, directory.resolve(pageObject + ".json"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        });
    }

    /** The test failed: what it saw is not a record of the page working. */
    public static void discard(Config config) {
        forEachPending(config, (pending, pageObject) -> Files.deleteIfExists(pending));
    }

    private interface PendingAction {
        void apply(Path pending, String pageObject) throws Exception;
    }

    private static void forEachPending(Config config, PendingAction action) {
        if (config == null)
            return;
        try {
            Path directory = pendingDirectory();
            if (!Files.isDirectory(directory))
                return;
            String prefix = testKey(config) + "__";
            try (java.util.stream.Stream<Path> entries = Files.list(directory)) {
                for (Path pending : entries.collect(java.util.stream.Collectors.toList())) {
                    String fileName = pending.getFileName().toString();
                    if (!fileName.startsWith(prefix) || !fileName.endsWith(".json"))
                        continue;
                    String pageObject = fileName.substring(prefix.length(),
                            fileName.length() - ".json".length());
                    try {
                        action.apply(pending, pageObject);
                    } catch (Throwable ignored) {
                        // One unusable file must not strand the rest.
                    }
                }
            }
        } catch (Throwable ignored) {
            // Baselines are an optimisation; never fail a test over one.
        }
    }

    private static Path baselineDirectory() {
        return Paths.get(DIR != null && !DIR.isEmpty()
                ? DIR
                : Config.resultsDirectory + File.separator + "baselines");
    }

    private static Path pendingDirectory() {
        return baselineDirectory().resolve("pending");
    }

    /** A filesystem-safe identifier for the test that recorded a fingerprint. */
    private static String testKey(Config config) {
        String test = (config.testcaseClass == null ? "" : config.testcaseClass)
                + "." + (config.testcaseName == null ? "" : config.testcaseName);
        return test.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String pendingName(Config config, String pageObject) {
        return testKey(config) + "__" + pageObject + ".json";
    }

    /**
     * A URL with its variable parts removed, so two visits to the same screen match.
     *
     * <p>Numeric and uuid-shaped path segments are the record being looked at, not the
     * screen being looked at, and a query string almost never changes which page it is.
     */
    static String shapeOf(String url) {
        if (url == null)
            return "";
        String withoutQuery = url.split("[?#]")[0];
        return withoutQuery
                .replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}", "/{uuid}")
                .replaceAll("/\\d+", "/{id}");
    }

    private static String bodyClass(Config config) {
        try {
            Object value = config.page.evaluate(
                    "() => document.body ? document.body.className : ''");
            return value == null ? "" : String.valueOf(value);
        } catch (Throwable e) {
            return "";
        }
    }

    /** Visits every Locator field, using the same reflection rule as FailureContext. */
    private static void forEachLocator(Object pageObject, LocatorVisitor visitor) {
        for (Class<?> type = pageObject.getClass();
             type != null && type != Object.class && type != BasePage.class;
             type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!Locator.class.isAssignableFrom(field.getType()))
                    continue;
                try {
                    field.setAccessible(true);
                    Locator locator = (Locator) field.get(pageObject);
                    if (locator != null)
                        visitor.visit(field.getName(), locator);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private interface LocatorVisitor {
        void visit(String name, Locator locator);
    }

    /**
     * Per-locator match counts, and what each locator actually matched.
     *
     * <p>A count answers "did this still resolve last time". It cannot answer "what did it
     * resolve to", so a renamed or moved element leaves nothing to compare against and the
     * diagnosis downstream is reduced to guessing from the selector string. The fingerprint
     * is that missing half: tag, role, accessible name, text, attributes, neighbouring text
     * and geometry, captured while the locator still worked.
     *
     * <p>Only unambiguous matches are recorded. A locator matching two elements has not told
     * us which one the test meant, and writing either one down would invent a fact.
     *
     * <p>One page snapshot serves every field: the same walk that produced the element list
     * also answers where a given element sits in it, so the indices cannot disagree.
     */
    private static void collect(Config config, Object pageObject,
                                Map<String, Object> counts, Map<String, Object> prints,
                                List<Object> landmarks) {
        String js = FINGERPRINTS ? LocatorCapture.script() : "";
        java.util.List<?> elements = null;
        if (!js.isEmpty()) {
            try {
                Object snapshot = LocatorCapture.snapshot(config.page);
                if (snapshot instanceof Map) {
                    Object found = ((Map<?, ?>) snapshot).get("elements");
                    if (found instanceof java.util.List)
                        elements = (java.util.List<?>) found;
                    Object marks = ((Map<?, ?>) snapshot).get("landmarks");
                    if (marks instanceof java.util.List)
                        landmarks.addAll((java.util.List<?>) marks);
                }
            } catch (Throwable ignored) {
                // Unusable snapshot: counts are still worth recording.
            }
        }
        final java.util.List<?> all = elements;

        forEachLocator(pageObject, (name, locator) -> {
            int count;
            try {
                count = locator.count();
            } catch (Throwable e) {
                return;
            }
            counts.put(name, count);
            if (all == null || count != 1)
                return;
            try {
                Object index = config.page.evaluate(js, locator.elementHandle(
                        new Locator.ElementHandleOptions().setTimeout(2000)));
                if (index instanceof Number) {
                    int i = ((Number) index).intValue();
                    if (i >= 0 && i < all.size())
                        prints.put(name, all.get(i));
                }
            } catch (Throwable ignored) {
                // One unfingerprintable locator must not cost us the others.
            }
        });
    }
}
