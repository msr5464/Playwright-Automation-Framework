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
 * Produce a Playwright storage state by running a module's OWN login code, so an
 * agent can drive a signed-in browser without being handed a password.
 *
 * <p><b>Arguments are property KEYS, never values</b> — resolved to credentials
 * inside this JVM. A password on the command line is visible in {@code ps}.
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
 * <p>A login that throws can still have authenticated — the destination page object
 * asserts itself loaded and may be the very thing that changed. So the state is saved
 * whenever the context holds cookies, flagged {@code degraded} with the error.
 *
 * <p>One line of output is contractual: {@code MINT_RESULT {...}}.
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

            // TestBase's @DataProvider normally stamps these, and the framework assumes
            // it happened (ApiHelper keys a ConcurrentHashMap on testcaseName).
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
                // Distinguished from the login failure below: both surface as
                // InvocationTargetException, but this one happens before any browser opens.
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                cause.printStackTrace();
                emit(false, "", 0, "could not construct " + helperName + ": "
                        + cause.getMessage() + originOf(cause));
                System.exit(1);
                return;
            }

            // Throws on a rejected credential — and on a changed destination page.
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

            // No cookies means the credential never took; saving that yields a file
            // that looks like a session and drops the next browser onto a login page.
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

    /** {@code ok} = state written; {@code degraded} = written despite the login throwing. */
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
