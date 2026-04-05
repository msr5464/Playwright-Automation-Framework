package automation.core;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates a TestNG XML at runtime from CLI parameters and executes it.
 *
 * Preferred usage — named system properties (AI agent safe):
 *   mvn test -DprojectName=Cards -Denvironment=staging -Dbrowser=chromium \
 *             -Dgroups=regression -Dcountry=SG
 *
 * Or via the shell wrapper:
 *   ./ai-run.sh --feature Cards --env staging --type api --country SG
 *
 * Legacy positional args (still supported for backward compatibility):
 *   mvn exec:java -Dexec.mainClass="automation.core.GenerateTestngXmlAndRun" \
 *     -Dexec.args="Cards qa-1 chromium regression SG EN false false false"
 *
 * Named properties take priority over positional args when both are supplied.
 */
public class GenerateTestngXmlAndRun
{

    private static final String GENERATED_XML_PATH = "generated-testng.xml";

    public static void main(String... args)
    {
        // Named -D system properties (preferred, AI agent safe).
        // Fall back to positional args for backward compatibility.
        String projectName           = sysPropOrArg("projectName",             args, 0, "CustomerFrontend");
        String environment           = sysPropOrArg("environment",             args, 1, "qa-1");
        String browserName           = sysPropOrArg("browser",                 args, 2, "chromium");
        String groupNames            = sysPropOrArg("groups",                  args, 3, "regression");
        String country               = sysPropOrArg("country",                 args, 4, "SG");
        String appLanguage           = sysPropOrArg("appLanguage",             args, 5, "EN");
        boolean debugMode            = Boolean.parseBoolean(sysPropOrArg("isDebugMode",           args, 6, "false"));
        boolean uploadToTestrail     = Boolean.parseBoolean(sysPropOrArg("uploadToTestrail",      args, 7, "false"));
        boolean isBrowserStackExecution = Boolean.parseBoolean(sysPropOrArg("isBrowserStackExecution", args, 8, "false"));

        System.out.println("=== Jarvis2 Test Runner ===");
        System.out.println("Project     : " + projectName);
        System.out.println("Environment : " + environment);
        System.out.println("Browser     : " + browserName);
        System.out.println("Groups      : " + groupNames);
        System.out.println("Country     : " + country);
        System.out.println("Language    : " + appLanguage);
        System.out.println("Debug       : " + debugMode);
        System.out.println("TestRail    : " + uploadToTestrail);
        System.out.println("BrowserStack: " + isBrowserStackExecution);
        System.out.println("===========================");

        // Propagate as system properties so Config picks them up
        System.setProperty("projectName",              projectName);
        System.setProperty("environment",              environment);
        System.setProperty("browser",                  browserName);
        System.setProperty("groupName",                groupNames);
        System.setProperty("country",                  country.toLowerCase());
        System.setProperty("appLanguage",              appLanguage.toLowerCase());
        System.setProperty("isDebugMode",              String.valueOf(debugMode));
        System.setProperty("uploadToTestrail",         String.valueOf(uploadToTestrail));
        System.setProperty("isBrowserStackExecution",  String.valueOf(isBrowserStackExecution));

        // Detect platform from group name
        String platform = "web";
        int threadCount = 10;
        if (groupNames.toLowerCase().contains("android"))
        {
            platform = "android";
            threadCount = isBrowserStackExecution ? 3 : 5;
        }
        else if (groupNames.toLowerCase().contains("ios"))
        {
            platform = "ios";
            threadCount = isBrowserStackExecution ? 3 : 5;
        }
        else if (groupNames.toLowerCase().contains("api"))
        {
            platform = "api";
            threadCount = 15;
        }

        // Build and run the suite
        XmlSuite suite = buildSuite(projectName, environment, browserName, platform, groupNames,
                country, appLanguage, threadCount, ParallelMode.TESTS);

        // Write XML to disk for reference / rerun
        writeXmlToDisk(suite);

        // Execute
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(List.of(suite));
        testNG.run();

        System.exit(testNG.getStatus());
    }

    /**
     * Builds an XmlSuite programmatically. Test classes are discovered by scanning
     * the compiled output for the given project name and groups.
     */
    public static XmlSuite buildSuite(String projectName, String environment, String browserName,
                                      String platform, String groupNames, String country, String appLanguage,
                                      int threadCount, ParallelMode parallelMode)
    {
        XmlSuite suite = new XmlSuite();
        suite.setName("Jarvis2-" + projectName);
        suite.setParallel(parallelMode);
        suite.setThreadCount(threadCount);
        suite.setVerbose(1);

        // Suite-level parameters (read by TestBase / Config)
        suite.getParameters().put("environment",  environment);
        suite.getParameters().put("browser",      browserName);
        suite.getParameters().put("country",      country.toLowerCase());
        suite.getParameters().put("appLanguage",  appLanguage.toLowerCase());
        suite.getParameters().put("projectName",  projectName);
        suite.getParameters().put("platform",     platform);

        // Discover test classes for the given project
        List<String> testClasses = discoverTestClasses(projectName, platform);

        if (testClasses.isEmpty())
        {
            System.out.println("[WARN] No test classes found for project: " + projectName + ", platform: " + platform);
            System.out.println("[WARN] Add your test classes to GenerateTestngXmlAndRun.discoverTestClasses()");
        }

        // Split groups into individual XmlTest nodes (one per group for cleaner reporting)
        List<String> groups = Arrays.stream(groupNames.split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .toList();

        for (String group : groups)
        {
            XmlTest test = new XmlTest(suite);
            test.setName(projectName + "-" + group);
            test.setVerbose(0);
            test.addIncludedGroup(group);

            List<XmlClass> xmlClasses = testClasses.stream()
                    .map(XmlClass::new)
                    .toList();
            test.setXmlClasses(xmlClasses);
        }

        return suite;
    }

    /**
     * Returns the list of fully-qualified test class names to include for a given project.
     * Add your project-specific test class mappings here.
     */
    private static List<String> discoverTestClasses(String projectName, String platform)
    {
        List<String> classes = new ArrayList<>();

        switch (projectName.toLowerCase())
        {
            case "saucedemo" ->
            {
                classes.add("automation.saucedemo.SauceDemoApiTest");
                classes.add("automation.saucedemo.SauceDemoWebTest");
            }
            case "github" ->
            {
                classes.add("automation.github.GitHubApiTest");
                classes.add("automation.github.GitHubLoginTest");
            }
            default ->
            {
                // No registered mapping — caller should see the WARN above
            }
        }

        // Filter by platform if needed
        if ("api".equals(platform))
        {
            classes = classes.stream().filter(c -> c.contains("Api")).toList();
        }
        else if ("android".equals(platform) || "ios".equals(platform))
        {
            classes = classes.stream().filter(c -> c.contains("Mobile") || c.contains("App")).toList();
        }

        return classes;
    }

    /**
     * Writes the generated XmlSuite to disk as generated-testng.xml for inspection and rerun.
     */
    private static void writeXmlToDisk(XmlSuite suite)
    {
        try (FileWriter fw = new FileWriter(new File(GENERATED_XML_PATH)))
        {
            fw.write(suite.toXml());
            System.out.println("Generated TestNG XML written to: " + GENERATED_XML_PATH);
        }
        catch (IOException e)
        {
            System.err.println("Failed to write generated-testng.xml: " + e.getMessage());
        }
    }

    /**
     * Returns the value of a named system property if set, otherwise falls back to
     * the positional CLI arg at {@code index}, otherwise returns {@code defaultValue}.
     *
     * This allows both invocation styles:
     *   Named:      mvn test -DprojectName=Cards          (preferred, AI agent safe)
     *   Positional: -Dexec.args="Cards staging chromium"  (legacy, backward compatible)
     */
    private static String sysPropOrArg(String propertyName, String[] args, int index, String defaultValue)
    {
        String sysProp = System.getProperty(propertyName);
        if (sysProp != null && !sysProp.isEmpty())
        {
            return sysProp;
        }
        if (args.length > index && args[index] != null && !args[index].isEmpty())
        {
            return args[index];
        }
        return defaultValue;
    }
}
