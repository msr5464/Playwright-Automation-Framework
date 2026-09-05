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

    /** -Dbaseline.dir, then HEALING_BASELINE_DIR, then baselineDir, then test-output/baselines. */
    private static final String DIR_PROPERTY = "baselineDir";
    private static final String DIR_ENV = "HEALING_BASELINE_DIR";
    private static final String DIR_SYSTEM = "baseline.dir";

    private static final Set<String> WRITTEN = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Opt out of the per-page-object DOM walk; coverage counts are recorded either way. */
    private static final String FINGERPRINTS_PROPERTY = "baselineFingerprints";
    private static final String FINGERPRINTS_ENV = "HEALING_BASELINE_FINGERPRINTS";
    private static final String FINGERPRINTS_SYSTEM = "baseline.fingerprints";

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
            // Headings and landmark roles: a better "right screen?" check than a
            // URL, which survives a redirect to a login page unchanged.
            root.put("landmarks", landmarks);

            Path directory = pendingDirectory(config);
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
            Path directory = baselineDirectory(config);
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
            Path directory = pendingDirectory(config);
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

    /** First source that answers: -D system property, environment, properties, null. */
    private static String setting(Config config, String systemKey, String envKey,
                                  String propertyKey) {
        String value = System.getProperty(systemKey);
        if (value == null || value.isEmpty())
            value = System.getenv(envKey);
        if ((value == null || value.isEmpty()) && config != null)
            value = config.runTimeProperties.getProperty(propertyKey);
        return value == null || value.isEmpty() ? null : value;
    }

    private static boolean fingerprintsEnabled(Config config) {
        return !"false".equalsIgnoreCase(
                setting(config, FINGERPRINTS_SYSTEM, FINGERPRINTS_ENV, FINGERPRINTS_PROPERTY));
    }

    private static Path baselineDirectory(Config config) {
        String configured = setting(config, DIR_SYSTEM, DIR_ENV, DIR_PROPERTY);
        return Paths.get(configured != null
                ? configured
                : Config.resultsDirectory + File.separator + "baselines");
    }

    private static Path pendingDirectory(Config config) {
        return baselineDirectory(config).resolve("pending");
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
     * Per-locator match counts, plus a fingerprint of what each one matched:
     * tag, role, accessible name, text, attributes, neighbours, geometry.
     *
     * <p>Snapshot and indices must come from ONE evaluate — resolving indices in a
     * second walk picks up a DOM that has moved on, silently fingerprinting the
     * wrong element.
     */
    private static void collect(Config config, Object pageObject,
                                Map<String, Object> counts, Map<String, Object> prints,
                                List<Object> landmarks) {
        String js = fingerprintsEnabled(config) ? LocatorCapture.script() : "";
        // Resolve every handle first, then one evaluate. count != 1 is skipped: an
        // ambiguous locator has not said which element the test meant.
        final List<String> names = new java.util.ArrayList<>();
        final List<Object> handles = new java.util.ArrayList<>();
        forEachLocator(pageObject, (name, locator) -> {
            int count;
            try {
                count = locator.count();
            } catch (Throwable e) {
                return;
            }
            counts.put(name, count);
            if (js.isEmpty() || count != 1)
                return;
            try {
                Object handle = locator.elementHandle(
                        new Locator.ElementHandleOptions().setTimeout(2000));
                if (handle != null) {
                    names.add(name);
                    handles.add(handle);
                }
            } catch (Throwable ignored) {
                // One unresolvable locator must not cost us the others.
            }
        });
        if (js.isEmpty())
            return;

        try {
            Object result = config.page.evaluate(js, handles);
            if (!(result instanceof Map))
                return;
            Map<?, ?> snap = (Map<?, ?>) result;
            Object found = snap.get("elements");
            Object idx = snap.get("indices");
            if (!(found instanceof List))
                return;
            List<?> all = (List<?>) found;
            List<?> indices = (idx instanceof List) ? (List<?>) idx : Collections.emptyList();

            Object marks = snap.get("landmarks");
            if (marks instanceof List)
                landmarks.addAll((List<?>) marks);

            for (int n = 0; n < names.size() && n < indices.size(); n++) {
                if (!(indices.get(n) instanceof Number))
                    continue;
                int i = ((Number) indices.get(n)).intValue();
                if (i >= 0 && i < all.size())
                    prints.put(names.get(n), all.get(i));
            }
        } catch (Throwable ignored) {
            // An unusable snapshot costs the fingerprints, never the counts.
        }
    }
}
