package automation.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Checks that a promotion keeps what the baseline it replaces knew.
 *
 * <p>A promotion swaps the whole file. A passing run whose capture missed — a header that
 * re-rendered under the handle, a frame caught mid-navigation — therefore wrote a record
 * with no fingerprints over one that had them, and the next time one of those locators
 * broke there was nothing left to compare the page against. The same swap dropped the
 * repair history, which only the healing agent writes.
 *
 * <p>No browser: this is the merge, not the capture. Run it with
 * {@code mvn -q test-compile && java -cp "target/classes:target/test-classes:$(mvn
 * -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)"
 * automation.core.FingerprintCarryProbe}.
 */
public class FingerprintCarryProbe {

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("fingerprint-probe");
        Path pending = dir.resolve("pending.json");
        Path target = dir.resolve("CheckoutPage.json");

        // The run that fingerprinted both locators.
        write(target, "{\"recordedAt\":\"2026-09-22T08:28:36\","
                + "\"selectors\":{\"total\":\"Locator@div.total\",\"pay\":\"Locator@#pay\"},"
                + "\"fingerprints\":{\"total\":{\"tag\":\"div\"},\"pay\":{\"tag\":\"button\"}},"
                + "\"healHistory\":{\"pay\":[{\"healedAt\":\"2026-09-20T10:00:00\"}]}}");
        // A later passing run whose capture came back empty.
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"selectors\":{\"total\":\"Locator@div.total\",\"pay\":\"Locator@#pay\"},"
                + "\"fingerprints\":{}}");
        Baseline.carryForwardFingerprints(pending, target);
        expect(section(pending, "fingerprints").containsKey("total")
                        && section(pending, "fingerprints").containsKey("pay"),
                "a capture that missed must not erase the fingerprints already on record");
        expect(section(pending, "healHistory").containsKey("pay"),
                "the repair history is written by the healer, so a test run must not drop it");

        // What this run did capture wins over what was there.
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"selectors\":{\"total\":\"Locator@div.total\",\"pay\":\"Locator@#pay\"},"
                + "\"fingerprints\":{\"total\":{\"tag\":\"span\"}}}");
        Baseline.carryForwardFingerprints(pending, target);
        expect("span".equals(((Map<?, ?>) section(pending, "fingerprints").get("total")).get("tag")),
                "a fresh fingerprint must replace the old one, not be replaced by it");
        expect(section(pending, "fingerprints").containsKey("pay"),
                "the field this run missed still keeps its earlier fingerprint");

        // A locator that changed is a different element: its old fingerprint describes
        // whatever the previous selector matched, and keeping it would point a later
        // repair at the wrong thing.
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"selectors\":{\"total\":\"Locator@div.order-total\",\"pay\":\"Locator@#pay\"},"
                + "\"fingerprints\":{}}");
        Baseline.carryForwardFingerprints(pending, target);
        expect(!section(pending, "fingerprints").containsKey("total"),
                "a fingerprint must not outlive the locator it was taken for");
        expect(section(pending, "fingerprints").containsKey("pay"),
                "an unchanged locator keeps its fingerprint");

        // A record written before selectors were kept gives nothing to compare, so
        // nothing is carried: unknown is not the same as unchanged.
        write(target, "{\"recordedAt\":\"2026-09-22T08:28:36\","
                + "\"fingerprints\":{\"total\":{\"tag\":\"div\"}}}");
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"selectors\":{\"total\":\"Locator@div.total\"},\"fingerprints\":{}}");
        Baseline.carryForwardFingerprints(pending, target);
        expect(!section(pending, "fingerprints").containsKey("total"),
                "without a recorded selector there is no saying the locator is unchanged");

        // A locator fingerprinted in use resolved, whatever the page-load count says.
        write(target, "{\"recordedAt\":\"2026-09-22T08:28:36\",\"coverage\":{\"overlayName\":0}}");
        write(pending, "{\"recordedAt\":\"2026-09-23T11:17:34\","
                + "\"coverage\":{\"overlayName\":0},"
                + "\"fingerprints\":{\"overlayName\":{\"tag\":\"div\"}}}");
        Baseline.carryForwardLastSeen(pending, target);
        expect("2026-09-23T11:17:34".equals(section(pending, "lastSeen").get("overlayName")),
                "a locator the test used successfully was seen, even if it was not there at load");

        System.out.println("FingerprintCarryProbe: all checks passed");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Path file, String key) throws Exception {
        Object value = new ObjectMapper().readValue(file.toFile(), Map.class).get(key);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private static void write(Path file, String json) throws Exception {
        Files.write(file, json.getBytes(StandardCharsets.UTF_8));
    }

    private static void expect(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
