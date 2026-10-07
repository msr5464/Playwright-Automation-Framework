package automation.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Checks that a promotion keeps the evidence a locator was once on the page.
 *
 * <p>{@code coverage} counts every locator declared on a page object, including the ones
 * the passing test never went near, so a selector that has always been wrong is recorded
 * as matching nothing on a run that passed. Read back on its own that is indistinguishable
 * from an element the product removed — and the healing agent stops for the second, where
 * the first is a one-word locator fix. {@code lastSeen} is what tells them apart, and it
 * only works if it survives the promotion that replaces the file holding it.
 *
 * <p>No browser: this is the merge, not the capture. Run it with
 * {@code mvn -q test-compile && java -cp "target/classes:target/test-classes:$(mvn
 * -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)"
 * automation.core.LastSeenProbe}.
 */
public class LastSeenProbe {

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("lastseen-probe");
        Path pending = dir.resolve("pending.json");
        Path target = dir.resolve("ProductsPage.json");

        // The run that recorded the cart link working.
        write(target, "{\"recordedAt\":\"2026-09-22T08:28:36\","
                + "\"coverage\":{\"inventory\":1,\"cartLink\":1},"
                + "\"lastSeen\":{\"inventory\":\"2026-09-22T08:28:36\","
                + "\"cartLink\":\"2026-09-22T08:28:36\"}}");
        // A later run that passes without touching the cart, with the link broken.
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"coverage\":{\"inventory\":1,\"cartLink\":0}}");

        Baseline.carryForwardLastSeen(pending, target);
        Map<String, Object> merged = lastSeen(pending);

        expect("2026-09-22T08:28:36".equals(merged.get("cartLink")),
                "cartLink matched on 09-22, so the promotion must remember it did");
        expect("2026-09-23T11:17:34".equals(merged.get("inventory")),
                "inventory matched again, so its stamp must move forward");

        // A locator that has never matched anywhere stays out: absent from lastSeen
        // is exactly how the reader tells "never worked" from "was taken away".
        write(target, "{\"recordedAt\":\"2026-09-22T08:28:36\","
                + "\"coverage\":{\"inventory\":1},\"lastSeen\":{\"inventory\":\"2026-09-22T08:28:36\"}}");
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"coverage\":{\"inventory\":1,\"typoLink\":0}}");
        Baseline.carryForwardLastSeen(pending, target);
        // Still counted under `coverage`, deliberately — it is only `lastSeen` that
        // must stay silent about a locator no run has ever resolved.
        expect(!lastSeen(pending).containsKey("typoLink"),
                "a selector that never matched must not appear in lastSeen");

        System.out.println("LastSeenProbe: ok");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> lastSeen(Path path) throws Exception {
        Map<String, Object> root = new ObjectMapper().readValue(path.toFile(), Map.class);
        Object seen = root.get("lastSeen");
        return seen instanceof Map ? (Map<String, Object>) seen : java.util.Collections.emptyMap();
    }

    private static void write(Path path, String json) throws Exception {
        Files.write(path, json.getBytes(StandardCharsets.UTF_8));
    }

    private static void expect(boolean condition, String what) {
        if (!condition)
            throw new AssertionError(what);
    }
}
