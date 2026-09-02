package automation.core;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ViewportSize;

import automation.core.Enums.ProjectName;
import automation.core.Enums.TraceMode;
import automation.core.Enums.VideoMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BrowserHelper {

    public static void initBrowser(Config config) {
        String browserName = config.getRunTimeProperty("browserName", "chromium");
        boolean headless = !"false".equalsIgnoreCase(config.getRunTimeProperty("headless", "true"));

        // Skip browser init for API-only tests
        if ("api".equalsIgnoreCase(browserName)) {
            Log.comment(config, "API-only test - skipping browser initialization");
            return;
        }

        config.playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        // repairMode: keep the browser alive past the end of the run so a fixing
        // agent can attach to the ACTUAL failing page, with its session, test data
        // and mid-flow state intact.
        //
        // The browser must be launched as a DETACHED process that Playwright does
        // not own. Adding --remote-debugging-port to a normal launch() is not
        // enough: Playwright's driver kills every browser it started when the JVM
        // exits, so the "parked" browser was always dead by the time anything tried
        // to attach — verified by watching the CDP endpoint disappear the moment
        // Maven finished. Launching it ourselves and attaching with connectOverCDP
        // inverts that ownership, so the browser outlives the test process.
        if (Boolean.parseBoolean(config.getRunTimeProperty("repairMode", "false"))) {
            config.cdpPort = Integer.parseInt(config.getRunTimeProperty("repairCdpPort", "9222"));
            if (launchDetachedBrowserForRepair(config)) {
                return;   // config.browser/context/page are set by the helper
            }
            Log.warning(config, "Repair mode requested but the detached browser could not "
                    + "be started — falling back to a normal run");
            config.cdpPort = 0;
        }

        // Slow motion for debugging
        String slowMo = config.getRunTimeProperty("slowMo");
        if (slowMo != null && !slowMo.isEmpty()) {
            launchOptions.setSlowMo(Double.parseDouble(slowMo));
        }

        config.browser = switch (browserName.toLowerCase()) {
            case "firefox" -> config.playwright.firefox().launch(launchOptions);
            case "webkit" -> config.playwright.webkit().launch(launchOptions);
            default -> config.playwright.chromium().launch(launchOptions);
        };

        // Browser context with optional video recording
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(1920, 1080));

        configureVideoRecording(config, contextOptions);

        config.browserContext = config.browser.newContext(contextOptions);

        startTracing(config);

        config.page = config.browserContext.newPage();

        installFlightRecorder(config);

        // Set timeouts (reuse WaitHelper's centralised timeout calculation)
        int timeoutMs = WaitHelper.getTimeout(config);
        config.page.setDefaultTimeout(timeoutMs);
        config.page.setDefaultNavigationTimeout(timeoutMs * 3L);

        Log.comment(config, "Browser initialized: " + browserName + " (headless=" + headless + ")");
    }


    /**
     * Which cookies in a stored session have already expired, if any.
     *
     * <p>A count of what is still valid proves nothing: an expired session usually keeps
     * plenty of good cookies while the two that actually authenticate are dead. So this
     * names them, and returns an empty string when there is nothing to report.
     *
     * <p>Cookies with no expiry, or a negative one, are session cookies. They die with the
     * browser and carry nothing to check.
     */
    static String expiredCookiesIn(Path sessionPath) {
        try {
            com.fasterxml.jackson.databind.JsonNode root =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(sessionPath.toFile());
            com.fasterxml.jackson.databind.JsonNode cookies = root.get("cookies");
            if (cookies == null || !cookies.isArray())
                return "";

            long now = System.currentTimeMillis() / 1000L;
            List<String> expired = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode cookie : cookies) {
                com.fasterxml.jackson.databind.JsonNode expires = cookie.get("expires");
                if (expires == null || !expires.isNumber() || expires.asDouble() < 0)
                    continue;
                if (expires.asDouble() < now) {
                    String name = cookie.hasNonNull("name") ? cookie.get("name").asText() : "?";
                    expired.add(name + " expired "
                            + java.time.Instant.ofEpochSecond((long) expires.asDouble())
                                    .atZone(java.time.ZoneOffset.UTC).toLocalDate());
                }
            }
            if (expired.isEmpty())
                return "";
            return expired.size() + " of " + cookies.size() + " cookies have expired ("
                    + String.join(", ", expired.subList(0, Math.min(4, expired.size()))) + ")";
        } catch (Throwable e) {
            // An unreadable session is a separate problem; do not block the run on it.
            return "";
        }
    }

    /**
     * Install the flight recorder on a freshly created page.
     *
     * <p>Every test gets this, with no per-test work, because the evidence that
     * distinguishes "the locator is stale" from "the page never loaded" has to be
     * collected while the page is alive. Reconstructing it afterwards is impossible: a
     * request that failed, an exception that broke rendering and a redirect that went
     * somewhere unexpected all leave the DOM looking simply, innocently, wrong.
     *
     * <p>Each list is capped, so a page that logs a warning on every mouse move cannot
     * grow them without bound. Listener bodies never throw.
     */
    private static void installFlightRecorder(Config config) {
        if (config == null || config.page == null)
            return;
        try {
            config.page.onResponse(response -> {
                try {
                    int status = response.status();
                    if (status >= 400)
                        record(config.httpErrors, status + " " + response.request().method()
                                + " " + response.url());
                } catch (Throwable ignored) {
                }
            });
            config.page.onRequestFailed(request -> {
                try {
                    record(config.httpErrors, "FAILED " + request.method() + " " + request.url()
                            + " (" + request.failure() + ")");
                } catch (Throwable ignored) {
                }
            });
            config.page.onConsoleMessage(message -> {
                try {
                    if ("error".equalsIgnoreCase(message.type()))
                        record(config.jsErrors, "console: " + message.text());
                } catch (Throwable ignored) {
                }
            });
            config.page.onPageError(error -> {
                try {
                    record(config.jsErrors, "uncaught: " + error);
                } catch (Throwable ignored) {
                }
            });
            config.page.onFrameNavigated(frame -> {
                try {
                    if (frame.equals(config.page.mainFrame()))
                        record(config.navigationHistory, frame.url());
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable e) {
            Log.debug(config, "Flight recorder not installed: " + e.getMessage());
        }
    }

    private static void record(java.util.List<String> sink, String entry) {
        synchronized (sink) {
            if (sink.size() < Config.FLIGHT_RECORDER_LIMIT)
                sink.add(entry);
        }
    }

    private static void configureVideoRecording(Config config, Browser.NewContextOptions contextOptions) {
        VideoMode videoMode = VideoMode.fromString(config.getRunTimeProperty("VideoMode"));
        if (videoMode == VideoMode.ON || videoMode == VideoMode.ON_FAILURE) {
            String videoDir = Config.resultsDirectory + File.separator + "videos";
            new File(videoDir).mkdirs();
            contextOptions.setRecordVideoDir(Paths.get(videoDir))
                    .setRecordVideoSize(1280, 720);
        }
    }

    public static void takeScreenshot(Config config) {
        if (config.page == null)
            return;
        try {
            String screenshotDir = Config.resultsDirectory + File.separator + "screenshots";
            new File(screenshotDir).mkdirs();
            String fileName = config.testcaseName + "_" + DataGenerator.getCurrentDateTime("HHmmss") + ".png";
            Path screenshotPath = Paths.get(screenshotDir, fileName);
            config.page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));
            String link = "<a href='" + screenshotPath
                    + "' target='_blank' style='color:#2563EB;'>&#128247; View Screenshot</a>";
            Log.comment(config, link);
        } catch (Exception e) {
            Log.warning(config, "Screenshot failed: " + e.getMessage());
        }
    }

    /**
     * Launch Chromium as a detached OS process and attach to it over CDP.
     *
     * Used only by repair mode. Playwright terminates browsers it launched when
     * the owning process exits, which is exactly what must NOT happen here — the
     * whole point is for the browser to still be sitting on the failing page after
     * the test run is over. Starting it ourselves and connecting with
     * connectOverCDP means Playwright is a client, not the owner, so
     * closeBrowser()'s early return actually leaves something behind.
     *
     * The PID is recorded on config so the repair session can publish it and
     * whoever attaches can terminate the browser when finished.
     *
     * Returns true when the browser is up and config.browser/context/page are set.
     */
    private static boolean launchDetachedBrowserForRepair(Config config) {
        try {
            String executable = config.getRunTimeProperty("repairChromePath", "");
            if (executable == null || executable.isEmpty()) {
                executable = findPlaywrightChromium();
            }
            if (executable == null) {
                Log.warning(config, "Repair mode: could not locate a Chromium binary "
                        + "(set -DrepairChromePath=/path/to/chrome)");
                return false;
            }

            // A normal run gets a fresh browser.newContext(); this one gets a Chrome
            // profile on disk, and mkdirs() on an existing directory keeps whatever is
            // already in it. Left alone it accumulates cookies across every repair run,
            // so the re-run starts authenticated while the test is written to start
            // logged out: the login page redirects away, the very first step fails, and
            // the browser parks on a page the test never meant to be on. Reproducing the
            // failure means reproducing the state it happened in, so the profile is
            // rebuilt each time.
            Path profileDir = Paths.get(Config.resultsDirectory, "repair-profile");
            deleteRecursively(profileDir.toFile());
            new File(profileDir.toString()).mkdirs();

            ProcessBuilder builder = new ProcessBuilder(
                    executable,
                    "--remote-debugging-port=" + config.cdpPort,
                    "--user-data-dir=" + profileDir,
                    "--no-first-run",
                    "--no-default-browser-check",
                    "--window-size=1920,1080");
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process browserProcess = builder.start();
            config.repairBrowserPid = browserProcess.pid();

            String cdpUrl = "http://localhost:" + config.cdpPort;
            if (!waitForCdp(cdpUrl, 20000)) {
                Log.warning(config, "Repair mode: CDP endpoint never came up at " + cdpUrl);
                browserProcess.destroyForcibly();
                config.repairBrowserPid = 0;
                return false;
            }

            config.playwright = Playwright.create();
            config.browser = config.playwright.chromium().connectOverCDP(cdpUrl);
            config.browserContext = config.browser.contexts().isEmpty()
                    ? config.browser.newContext()
                    : config.browser.contexts().get(0);

            startTracing(config);

            config.page = config.browserContext.pages().isEmpty()
                    ? config.browserContext.newPage()
                    : config.browserContext.pages().get(0);
            config.page.setViewportSize(1920, 1080);
            config.page.setDefaultTimeout(WaitHelper.getTimeout(config));

            Log.comment(config, "Repair mode ON — detached browser pid=" + config.repairBrowserPid
                    + ", CDP " + cdpUrl);
            return true;
        } catch (Exception e) {
            Log.warning(config, "Repair mode: detached launch failed: " + e.getMessage());
            return false;
        }
    }

    /** Best-effort recursive delete. A profile we cannot clear is not fatal. */
    private static void deleteRecursively(File target) {
        try {
            if (!target.exists()) {
                return;
            }
            File[] children = target.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
            target.delete();
        } catch (Throwable ignored) {
            // A stale profile degrades the repair session; failing here would take
            // down the run that was only trying to explain a failure.
        }
    }

    /** Newest Chromium that Playwright has already downloaded. */
    private static String findPlaywrightChromium() {
        Path cache = Paths.get(System.getProperty("user.home"), "Library", "Caches", "ms-playwright");
        if (!cache.toFile().isDirectory()) {
            cache = Paths.get(System.getProperty("user.home"), ".cache", "ms-playwright");
        }
        File[] builds = cache.toFile().listFiles(
                f -> f.isDirectory() && f.getName().startsWith("chromium-"));
        if (builds == null || builds.length == 0) {
            return null;
        }
        java.util.Arrays.sort(builds, java.util.Comparator.comparing(File::getName).reversed());
        for (File build : builds) {
            for (String rel : new String[] {
                    "chrome-mac/Chromium.app/Contents/MacOS/Chromium",
                    "chrome-linux/chrome",
                    "chrome-win/chrome.exe" }) {
                File candidate = new File(build, rel);
                if (candidate.canExecute()) {
                    return candidate.getAbsolutePath();
                }
            }
        }
        return null;
    }

    /** Poll the CDP endpoint until the detached browser answers, or time out. */
    private static boolean waitForCdp(String cdpUrl, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                        new java.net.URL(cdpUrl + "/json/version").openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                if (conn.getResponseCode() == 200) {
                    return true;
                }
            } catch (Exception ignored) {
                // not up yet
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Start a Playwright trace for this context.
     *
     * A trace records every action the test performed, with the selector it used,
     * plus a DOM snapshot and screenshot per step. For a broken locator that is
     * the whole flow rather than just the last page: which selectors worked,
     * which one failed, and what the page looked like at each point. Humans open
     * the zip in Playwright Trace Viewer; the QA agent network reads the action
     * timeline out of it.
     */
    public static void startTracing(Config config) {
        TraceMode traceMode = TraceMode.fromString(config.getRunTimeProperty("traceMode"));
        if (traceMode == TraceMode.OFF || config.browserContext == null)
            return;
        try {
            config.browserContext.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true)
                    .setTitle(config.testcaseName == null ? "test" : config.testcaseName));
            config.tracingStarted = true;
        } catch (Exception e) {
            Log.warning(config, "Could not start tracing: " + e.getMessage());
        }
    }

    /**
     * Stop tracing, keeping the zip only when this run is worth keeping.
     * Must be called while the context is still open.
     */
    public static void stopTracing(Config config) {
        TraceMode traceMode = TraceMode.fromString(config.getRunTimeProperty("traceMode"));
        // Not every init path traces: an init that skipped startTracing, or a start
        // that failed, must not turn teardown into a "Must start tracing before
        // stopping" error on an untraced context.
        if (traceMode == TraceMode.OFF || config.browserContext == null || !config.tracingStarted)
            return;
        config.tracingStarted = false;
        try {
            if (traceMode == TraceMode.ON_FAILURE && config.testResult) {
                config.browserContext.tracing().stop();  // discard: the test passed
                return;
            }
            String traceDir = Config.resultsDirectory + File.separator + "traces";
            new File(traceDir).mkdirs();
            String fileName = config.testcaseName + "_"
                    + DataGenerator.getCurrentDateTime("HHmmss") + ".zip";
            Path tracePath = Paths.get(traceDir, fileName);
            config.browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            config.tracePath = tracePath.toString();
            Log.comment(config, "<a href='" + tracePath
                    + "' target='_blank' style='color:#2563EB;'>&#128269; View Trace</a>");
        } catch (Exception e) {
            Log.warning(config, "Could not stop tracing: " + e.getMessage());
        }
    }

    /**
     * Save the page's rendered HTML at the moment of failure.
     *
     * A screenshot shows a human what broke; only the DOM shows an automated
     * fixer WHY a locator stopped matching. Capturing it here is the one moment
     * where the browser is guaranteed to be in the right session, with the right
     * test data, at the right step of the flow — state that is expensive or
     * impossible to reproduce afterwards by replaying the test.
     *
     * Written to {resultsDirectory}/dom/{testcaseName}_{HHmmss}.html with a
     * machine-readable header comment. Never throws: a failed capture must not
     * turn a test failure into a listener crash.
     */
    public static void captureDomSnapshot(Config config) {
        if (config.page == null)
            return;
        try {
            String domDir = Config.resultsDirectory + File.separator + "dom";
            new File(domDir).mkdirs();
            String fileName = config.testcaseName + "_" + DataGenerator.getCurrentDateTime("HHmmss") + ".html";
            Path domPath = Paths.get(domDir, fileName);

            String url = "";
            try {
                url = config.page.url();
            } catch (Exception ignored) {
                // A closed or crashed page still has usable content sometimes.
            }

            String header = "<!-- qa-agent-network:dom-snapshot"
                    + " test=\"" + config.testcaseName + "\""
                    + " url=\"" + url + "\""
                    + " capturedAt=\"" + DataGenerator.getCurrentDateTime("yyyy-MM-dd'T'HH:mm:ss") + "\""
                    + " -->\n";
            Files.write(domPath, (header + config.page.content()).getBytes(StandardCharsets.UTF_8));

            config.domSnapshotPath = domPath.toString();
            config.failureUrl = url;

            String link = "<a href='" + domPath
                    + "' target='_blank' style='color:#2563EB;'>&#128196; View DOM Snapshot</a>";
            Log.comment(config, link);
        } catch (Exception e) {
            Log.warning(config, "DOM snapshot failed: " + e.getMessage());
        }
    }

    public static void closeBrowser(Config config) {
        if (config.keepBrowserOpen) {
            // repairMode parked this browser on the failing page. Tearing it down
            // here would destroy the one thing the repair session exists to inspect.
            // The trace still has to be flushed while the context is reachable, and
            // Playwright must be disconnected cleanly — the browser is a detached
            // process, so disconnecting does not kill it.
            stopTracing(config);
            try {
                if (config.playwright != null) {
                    config.playwright.close();
                    config.playwright = null;
                }
            } catch (Exception e) {
                Log.debug(config, "Repair mode: disconnect issue: " + e.getMessage());
            }
            Log.comment(config, "Repair mode — browser left open at the failure point "
                    + "(pid " + config.repairBrowserPid + ", CDP http://localhost:"
                    + config.cdpPort + ")");
            return;
        }
        try {
            // Trace must be flushed while the context is still open.
            stopTracing(config);
            // Capture video path before closing — page.video().path() is only valid while
            // page is open
            VideoMode videoMode = VideoMode.fromString(config.getRunTimeProperty("VideoMode"));
            Path videoPath = null;
            if (videoMode != VideoMode.OFF && config.page != null && config.page.video() != null) {
                try {
                    videoPath = config.page.video().path();
                } catch (Exception e) {
                    Log.debug(config, "Could not get video path: " + e.getMessage());
                }
            }

            if (config.page != null) {
                config.page.close();
                config.page = null;
            }
            if (config.browserContext != null) {
                // Video file is finalized (written to disk) when the context closes
                config.browserContext.close();
                config.browserContext = null;
            }
            if (config.browser != null) {
                config.browser.close();
                config.browser = null;
            }
            if (config.playwright != null) {
                config.playwright.close();
                config.playwright = null;
            }

            // Log video link AFTER context is closed so the file is fully written
            handleVideoRetention(config, videoMode, videoPath);
        } catch (Exception e) {
            Log.error("Error closing browser: " + e.getMessage());
        }
    }

    private static void handleVideoRetention(Config config, VideoMode videoMode, Path videoPath) {
        if (videoMode == VideoMode.OFF || videoPath == null)
            return;
        try {
            if (videoMode == VideoMode.ON_FAILURE && config.testResult) {
                // Delete video for passing tests when mode is ON_FAILURE
                videoPath.toFile().delete();
                Log.debug(config, "Deleted video for passing test");
            } else {
                // Store path on config — TestBase.afterMethod logs it with the explicit
                // ITestResult so Reporter associates the link with the correct test entry.
                config.videoPath = videoPath.toString();
                System.out.println("[Video] Recorded: " + videoPath);
            }
        } catch (Exception e) {
            Log.warning(config, "Video handling error: " + e.getMessage());
        }
    }

    /**
     * Navigate to a URL
     */
    public static void navigateTo(Config config, String url) {
        if (config.page == null) {
            initBrowser(config);
        }
        Log.action(config, "Navigating to: " + url);
        config.page.navigate(url);
        WaitHelper.waitForPageLoad(config);
    }

    /**
     * Save the current browser session (cookies + localStorage) to a JSON file.
     * File is stored under src/test/resources/{moduleName}/loginStorage/.
     */
    public static void storeSession(Config config, ProjectName moduleName, String fileName) {
        try {
            String dir = Config.testResourcesPath + moduleName.name().toLowerCase() + "/loginStorage";
            new File(dir).mkdirs();
            Path filePath = Paths.get(dir, fileName);
            config.browserContext.storageState(
                    new BrowserContext.StorageStateOptions().setPath(filePath));
            Log.comment(config, "Session stored: " + filePath);
        } catch (Exception e) {
            Log.warning(config, "Failed to store session: " + e.getMessage());
        }
    }

    /**
     * Initialize browser and load a previously stored session from a JSON file.
     * File is read from src/test/resources/{moduleName}/loginStorage/.
     */
    public static void initBrowserWithStoredSession(Config config, ProjectName moduleName, String fileName) {
        String browserName = config.getRunTimeProperty("browserName", "chromium");
        boolean headless = !"false".equalsIgnoreCase(config.getRunTimeProperty("headless", "true"));

        config.playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        String slowMo = config.getRunTimeProperty("slowMo");
        if (slowMo != null && !slowMo.isEmpty()) {
            launchOptions.setSlowMo(Double.parseDouble(slowMo));
        }

        config.browser = switch (browserName.toLowerCase()) {
            case "firefox" -> config.playwright.firefox().launch(launchOptions);
            case "webkit" -> config.playwright.webkit().launch(launchOptions);
            default -> config.playwright.chromium().launch(launchOptions);
        };

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(new ViewportSize(1920, 1080));

        Path sessionPath = Paths.get(Config.testResourcesPath + moduleName.name().toLowerCase() + "/loginStorage",
                fileName);
        if (sessionPath.toFile().exists()) {
            String expiry = expiredCookiesIn(sessionPath);
            if (!expiry.isEmpty()) {
                // Continuing here is how this class of bug hides. The browser opens,
                // the page loads, and the first locator looked for on it takes the
                // blame for a session that died months ago.
                config.logFailToEndExecution("StoredSessionExpired: " + sessionPath.getFileName()
                        + " — " + expiry + ". Regenerate it by re-running the login test "
                        + "that produces it.");
            }
            contextOptions.setStorageStatePath(sessionPath);
            Log.comment(config, "Loaded stored session: " + sessionPath);
        } else {
            // Previously a warning, which let the run continue unauthenticated and
            // fail later somewhere unrelated.
            config.logFailToEndExecution("StoredSessionMissing: the test asked for "
                    + sessionPath + ", which does not exist, so it would run "
                    + "unauthenticated. Generate it by running the login test first.");
        }

        configureVideoRecording(config, contextOptions);

        config.browserContext = config.browser.newContext(contextOptions);

        startTracing(config);

        config.page = config.browserContext.newPage();

        installFlightRecorder(config);

        int timeoutMs = WaitHelper.getTimeout(config);
        config.page.setDefaultTimeout(timeoutMs);
        config.page.setDefaultNavigationTimeout(timeoutMs * 3L);

        Log.comment(config, "Browser initialized with stored session: " + browserName);
    }
}
