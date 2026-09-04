package automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class WaitHelper {

    public static int getTimeout(Config config) {
        String objectWaitTime = config.getRunTimeProperty("ObjectWaitTime");
        int timeout = objectWaitTime != null ? Integer.parseInt(objectWaitTime) : 30;
        return timeout * 1000;
    }

    public static boolean waitForElementToBeVisible(Config config, Locator locator, String elementName) {
        long startTime = System.currentTimeMillis();
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(getTimeout(config)));
            config.logCommentForDebugging("Element is visible: " + elementName);
            return true;
        } catch (Exception e) {
            // Not every exception here is a timeout — a strict-mode violation throws
            // at once, and "not visible after timeout" misreads it downstream.
            config.logWarning(describeWaitFailure(e, elementName,
                    System.currentTimeMillis() - startTime, getTimeout(config)));
            // Every interaction waits here first, so this is the one place that knows
            // which element an about-to-fail action was reaching for. Remembering it
            // costs nothing and is what lets the failure be written down as a fact
            // rather than reconstructed from the message afterwards.
            FailureContext.waitingOn(locator, elementName,
                    System.currentTimeMillis() - startTime, getTimeout(config), e);
            return false;
        }
    }

    /** Name what actually ended the wait: a wait that gave up early did not time out. */
    private static String describeWaitFailure(Exception e, String elementName,
                                              long elapsedMs, long budgetMs) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        String summary = FailureContext.summarizeError(message);
        if (message.toLowerCase().contains("strict mode violation")) {
            return "Locator for '" + elementName + "' matches more than one element, so "
                    + "no action can run on it: " + summary;
        }
        if (elapsedMs < budgetMs / 10) {
            return "Wait for '" + elementName + "' failed after " + elapsedMs + "ms of a "
                    + budgetMs + "ms budget (not a timeout): " + summary;
        }
        return "Element not visible after timeout: " + elementName;
    }

    public static boolean waitForOptionalElementToBeVisible(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(5000));
            config.logCommentForDebugging("Element is visible: " + elementName);
            return true;
        } catch (Exception e) {
            config.logCommentForDebugging("Optional element not visible after timeout: " + elementName);
            return false;
        }
    }

    public static boolean waitForElementToBeHidden(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(getTimeout(config)));
            config.logCommentForDebugging("Element is hidden: " + elementName);
            return true;
        } catch (Exception e) {
            config.logWarning("Element still visible after timeout: " + elementName);
            return false;
        }
    }

    public static boolean waitForElementToBeAttached(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(getTimeout(config)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean waitForElementToBeDetached(Config config, Locator locator, String elementName) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.DETACHED)
                    .setTimeout(getTimeout(config)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * How long the last wait took, and whether the page changed while it ran.
     *
     * <p>Read by {@link BasePage#assertPageLoaded} when the wait gives up. A page that
     * was byte-identical from the first poll to the last was settled and simply wrong;
     * one that kept changing was still loading and ran out of time. Those need opposite
     * fixes, and nothing else in the failure distinguishes them.
     *
     * <p>Per-thread because tests run in parallel.
     */
    static final ThreadLocal<Long> LAST_WAIT_MS = ThreadLocal.withInitial(() -> 0L);
    static final ThreadLocal<Boolean> LAST_WAIT_DOM_CHANGED = ThreadLocal.withInitial(() -> null);

    public static boolean waitForAnyElementToBeDisplayed(Config config, Locator... locators) {
        long startTime = System.currentTimeMillis();
        int timeout = getTimeout(config);

        // Sampled at each poll, and only the last two are compared. "Was the page
        // still changing when we ran out of patience" is the diagnostic question;
        // "did it change at any point since we started" answers yes on every page
        // that simply finished rendering after the first sample.
        int previous = 0;
        int latest = 0;
        boolean sampled = false;

        while (System.currentTimeMillis() - startTime < timeout) {
            for (Locator locator : locators) {
                try {
                    if (locator.isVisible()) {
                        LAST_WAIT_MS.set(System.currentTimeMillis() - startTime);
                        LAST_WAIT_DOM_CHANGED.set(null);
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
            int sample = fingerprint(config);
            if (sample != 0) {
                previous = latest;
                latest = sample;
                sampled = true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        LAST_WAIT_MS.set(System.currentTimeMillis() - startTime);
        LAST_WAIT_DOM_CHANGED.set(sampled && previous != 0 ? latest != previous : null);
        return false;
    }

    /**
     * A cheap digest of the current DOM. 0 means "could not tell", which callers must
     * keep distinct from "did not change".
     */
    private static int fingerprint(Config config) {
        try {
            Object length = config.page.evaluate(
                    "() => document.body ? document.body.innerHTML.length : 0");
            return length == null ? 0 : Integer.parseInt(String.valueOf(length));
        } catch (Throwable e) {
            return 0;
        }
    }

    /**
     * Wait until the page has landed on a URL. waitForNetworkIdle cannot do this —
     * it reports on the CURRENT document, so it returns before a submit's navigation
     * has even begun. Returns false rather than throwing; the caller usually
     * navigates next and should report its own error, not this one.
     */
    public static boolean waitForUrl(Config config, String urlPattern) {
        try {
            config.page.waitForURL(urlPattern,
                    new Page.WaitForURLOptions().setTimeout(getTimeout(config)));
            return true;
        } catch (Exception e) {
            config.logWarning("URL never became '" + urlPattern + "' within the timeout");
            return false;
        }
    }

    public static void waitForNetworkIdle(Config config) {
        try {
            config.page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
        } catch (Exception e) {
            config.logWarning("Network idle wait failed: " + e.getMessage());
        }
    }

    /**
     * Smart loading detection - waits for loading indicators to disappear.
     */
    public static void waitForLoadingComplete(Config config, Locator loadingIndicator) {
        try {
            if (loadingIndicator.isVisible()) {
                loadingIndicator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(getTimeout(config)));
            }
        } catch (Exception e) {
            config.logWarning("Loading indicator wait: " + e.getMessage());
        }
    }

    public static void waitForPageLoad(Config config) {
        try {
            config.page.waitForLoadState();
        } catch (Exception e) {
            Log.debug(config, "Page load state wait failed: " + e.getMessage());
        }
    }
}
