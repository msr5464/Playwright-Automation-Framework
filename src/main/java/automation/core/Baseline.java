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
 * <p>Written only on success, once per page object per JVM run, and never allowed to fail
 * a test: a baseline is an optimisation for a later diagnosis, not part of the run.
 */
public class Baseline {

    /** Overridable so CI can point at a path that survives between builds. */
    private static final String DIR = System.getenv("BASELINE_DIR");

    private static final Set<String> WRITTEN = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private Baseline() {
    }

    /** Fingerprint {@code pageObject} now that it has confirmed it loaded. */
    public static void record(Config config, Object pageObject) {
        if (config == null || config.page == null || pageObject == null)
            return;
        String name = pageObject.getClass().getSimpleName();
        if (!WRITTEN.add(name))
            return; // once per run is enough; the page does not change mid-suite

        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("pageObject", name);
            root.put("recordedAt", DataGenerator.getCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss"));
            root.put("urlShape", shapeOf(config.page.url()));
            root.put("title", config.page.title());
            root.put("bodyClass", bodyClass(config));
            root.put("coverage", coverage(pageObject));

            Path directory = Paths.get(DIR != null && !DIR.isEmpty()
                    ? DIR
                    : Config.resultsDirectory + File.separator + "baselines");
            new File(directory.toString()).mkdirs();
            Files.write(directory.resolve(name + ".json"),
                    MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                            .getBytes(StandardCharsets.UTF_8));
        } catch (Throwable ignored) {
            // A missing baseline only lowers confidence later. It is never fatal.
        }
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

    /** Per-locator match counts, using the same reflection rule as FailureContext. */
    private static Map<String, Object> coverage(Object pageObject) {
        Map<String, Object> counts = new LinkedHashMap<>();
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
                        counts.put(field.getName(), locator.count());
                } catch (Throwable ignored) {
                }
            }
        }
        return counts;
    }
}
