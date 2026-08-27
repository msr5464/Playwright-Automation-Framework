package automation.core;

import com.microsoft.playwright.BrowserContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Produce a Playwright storage state by running a module's OWN login code.
 *
 * <p>Agents that drive a browser need a logged-in context, and they must never be
 * handed a password — the tooling around them logs whole command lines. The
 * previous answer was to require {@link BrowserHelper#storeSession}, but that is
 * keyed on the {@code ProjectName} enum and has exactly one caller, so most
 * modules could never produce one. The answer after that was worse: transcribe
 * the login by scraping selectors out of a page object with a regex, which found
 * GitHub's {@code LoginPage} when asked for Naukri's and typed a Naukri password
 * into {@code #login_field}.
 *
 * <p>This runs the login instead of describing it. Point it at the helper and
 * method the test under adaptation already calls, and every awkward part comes
 * for free: the module's own navigation, whatever the page object does after
 * submit, dismissed modals, OTP handling. When the login flow changes, this
 * breaks in the same place the module's tests break, which is the point.
 *
 * <p><b>Arguments are property KEYS, never values.</b> They are resolved to
 * credentials inside this JVM via {@link Config#getRunTimeProperty}. A password
 * passed on the command line would be visible in {@code ps} and in any parent
 * process's log, which is the whole thing this exists to avoid.
 *
 * <pre>
 * mvn -q compile exec:java \
 *   -Dexec.mainClass=automation.core.SessionMinter \
 *   -Dmint.helper=automation.modules.naukari.NaukriProfileSummaryHelper \
 *   -Dmint.method=doLogin \
 *   -Dmint.argKeys=naukari.username,naukari.password \
 *   -Dmint.out=/abs/path/to/NaukariLoginStorage.json
 * </pre>
 *
 * <p>Verification is the framework's own. Page object constructors call
 * {@code assertPageLoaded}, which hard-stops through {@code Assert.fail}, so a
 * rejected credential throws out of the invoke below rather than quietly saving
 * a storage state that looks valid and is not.
 *
 * <p><b>A login method that throws can still have authenticated.</b> Login
 * helpers usually end by navigating somewhere and constructing a page object
 * that asserts itself loaded, so a broken destination page fails the whole call
 * even though the credential was accepted. Refusing to save the session then is
 * circular: an adaptation agent needs a signed-in browser precisely so it can go
 * and look at the page that stopped matching. So the state is saved whenever the
 * context actually holds cookies, and the post-login error is reported alongside
 * it as {@code degraded} rather than swallowed — it is usually evidence about
 * the very change being adapted.
 *
 * <p>One line of output is contractual: {@code MINT_RESULT {...}}. Everything
 * else on stdout is ordinary framework logging.
 */
public final class SessionMinter
{
    /** The one line callers parse. Framework logging also emits braces. */
    private static final String MARKER = "MINT_RESULT ";

    private SessionMinter() { }

    public static void main(String[] args)
    {
        String helperName = property("mint.helper");
        String methodName = property("mint.method");
        String out        = property("mint.out");
        String argKeys    = System.getProperty("mint.argKeys", "").trim();

        if (helperName.isEmpty() || methodName.isEmpty() || out.isEmpty())
        {
            emit(false, "", 0, "mint.helper, mint.method and mint.out are all required");
            System.exit(2);
        }

        Config config = null;
        try
        {
            config = new Config();

            List<String> keys = new ArrayList<>();
            for (String key : argKeys.split(","))
            {
                if (!key.trim().isEmpty()) keys.add(key.trim());
            }

            // Resolve keys to credentials here, inside the JVM. Anything missing is
            // reported by KEY NAME — never by value.
            Object[] values = new Object[keys.size()];
            Class<?>[] types = new Class<?>[keys.size()];
            List<String> unresolved = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++)
            {
                String value = config.getRunTimeProperty(keys.get(i));
                if (value == null || value.isEmpty()) unresolved.add(keys.get(i));
                values[i] = value;
                types[i] = String.class;
            }
            if (!unresolved.isEmpty())
            {
                emit(false, "", 0, "no value for " + String.join(", ", unresolved)
                        + " in the properties for environment '" + Config.environment + "'");
                System.exit(1);
            }

            // TestBase's @DataProvider stamps these onto every Config before a test
            // touches a helper, and parts of the framework assume it happened:
            // ApiHelper's constructor keys shared auth state on testcaseName and
            // hands it to a ConcurrentHashMap, which rejects a null key. Running a
            // helper outside TestNG means reproducing that setup, not skipping it.
            config.testcaseName = "SessionMinter";
            config.testcaseClass = SessionMinter.class.getSimpleName();

            Class<?> helperClass = Class.forName(helperName);
            Method login = helperClass.getMethod(methodName, types);

            Object helper;
            try
            {
                helper = helperClass.getConstructor(Config.class).newInstance(config);
            }
            catch (InvocationTargetException e)
            {
                // Constructing the helper and running the login both surface as
                // InvocationTargetException. Reporting them the same way sends
                // whoever reads it looking at the login for a failure that happened
                // before any browser opened.
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                cause.printStackTrace();
                emit(false, "", 0, "could not construct " + helperName + ": "
                        + cause.getMessage() + originOf(cause));
                System.exit(1);
                return;
            }

            // Throws on a rejected credential — and also when the page the login
            // lands on has itself changed, which is not the same thing.
            String loginError = "";
            try
            {
                login.invoke(helper, values);
            }
            catch (InvocationTargetException e)
            {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                cause.printStackTrace();
                loginError = cause.getMessage() + originOf(cause);
            }

            if (config.browserContext == null)
            {
                emit(false, "", 0, methodName + " opened no browser context"
                        + (loginError.isEmpty() ? " — nothing to save" : ": " + loginError));
                System.exit(1);
            }

            int cookies = config.browserContext.cookies().size();
            String landedOn = config.page != null ? config.page.url() : "";

            // No cookies means the credential never took. Saving that would produce
            // a file that looks like a session and drops the next browser onto a
            // login page — the failure this whole mechanism exists to prevent.
            if (!loginError.isEmpty() && cookies == 0)
            {
                emit(false, landedOn, 0, "login failed before authenticating: " + loginError);
                System.exit(1);
            }

            Path target = Paths.get(out).toAbsolutePath();
            if (target.getParent() != null) Files.createDirectories(target.getParent());
            config.browserContext.storageState(
                    new BrowserContext.StorageStateOptions().setPath(target));

            emit(true, landedOn, cookies, loginError);
        }
        catch (ClassNotFoundException e)
        {
            emit(false, "", 0, "no such helper class: " + helperName);
            System.exit(1);
        }
        catch (NoSuchMethodException e)
        {
            emit(false, "", 0, helperName + " has no method " + methodName
                    + " taking " + (argKeys.isEmpty() ? "no arguments"
                    : argKeys.split(",").length + " String argument(s)"));
            System.exit(1);
        }
        catch (Exception e)
        {
            emit(false, "", 0, e.getClass().getSimpleName() + ": " + e.getMessage());
            System.exit(1);
        }
        finally
        {
            if (config != null)
            {
                try { BrowserHelper.closeBrowser(config); } catch (Exception ignored) { }
            }
        }
        System.exit(0);
    }

    /** The first frame inside the framework, so a bare NPE still says where. */
    private static String originOf(Throwable cause)
    {
        for (StackTraceElement frame : cause.getStackTrace())
        {
            if (frame.getClassName().startsWith("automation."))
            {
                return " (at " + frame.getClassName() + "." + frame.getMethodName()
                        + ":" + frame.getLineNumber() + ")";
            }
        }
        return "";
    }

    private static String property(String key)
    {
        return System.getProperty(key, "").trim();
    }

    /**
     * The one contractual line. {@code ok} means a storage state was written;
     * {@code degraded} means it was written despite the login method throwing,
     * and {@code error} then describes what went wrong after authenticating.
     */
    private static void emit(boolean ok, String url, int cookies, String error)
    {
        System.out.println(MARKER + "{\"ok\":" + ok
                + ",\"degraded\":" + (ok && !error.isEmpty())
                + ",\"cookies\":" + cookies
                + ",\"url\":\"" + escape(url) + "\""
                + ",\"error\":\"" + escape(error) + "\"}");
        System.out.flush();
    }

    private static String escape(String value)
    {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray())
        {
            switch (c)
            {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> { if (c >= 0x20) sb.append(c); else sb.append(' '); }
            }
        }
        return sb.toString();
    }
}
