package automation.core;

import automation.core.Enums.DatabaseName;
import automation.core.Enums.QueryType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a TestNG XML at runtime from CLI parameters and executes it.
 * After execution, inserts results into the QA Dashboard DB, sends Slack and email notifications.
 *
 * Preferred usage — named system properties:
 *   mvn test -DprojectName=SauceDemo -Denvironment=staging -Dbrowser=chromium \
 *             -Dgroups=regression -Dcountry=SG
 *
 * Legacy positional args (still supported):
 *   mvn exec:java -Dexec.mainClass="automation.core.GenerateTestngXmlAndRun" \
 *     -Dexec.args="SauceDemo staging chromium regression SG EN main Build-1 null false false false false false"
 */
public class GenerateTestngXmlAndRun
{
    // ---- Public flags (readable by other classes) ----
    public static boolean isDebugMode           = false;
    public static boolean isRemoteExecution     = false;
    public static boolean isBrowserStackExecution = false;

    // ---- Shared state across methods (mirrors Thanos pattern) ----
    private static String jobBuildTag    = "Build-1";
    private static Date   startDate     = new Date();
    private static String passPercentage = null;
    private static String reportContents = null;
    private static String resultLink     = null;
    private static String resultsDirectory = null;

    private static int variableCount = 1;

    // ============================================================================================
    // MAIN
    // ============================================================================================

    public static void main(String... args)
    {
        try
        {
            System.out.println("Total params passed = " + args.length);

            // Parse parameters — named -D system properties take priority over positional args
            String projectName          = checkIfEmpty("projectName",          sysPropOrArg("projectName",          args, 0,  "SauceDemo"));
            String environment          = checkIfEmpty("environment",          sysPropOrArg("environment",          args, 1,  "staging"));
            String browserName          = checkIfEmpty("browserName",          sysPropOrArg("browserName",          args, 2,  "chromium"));
            String groupNames           = checkIfEmpty("groups",               sysPropOrArg("groups",               args, 3,  "regression"));
            String country              = checkIfEmpty("country",              sysPropOrArg("country",              args, 4,  "SG"));
            String appLanguage          = checkIfEmpty("appLanguage",          sysPropOrArg("appLanguage",          args, 5,  "EN"));
            String branchName           = checkIfEmpty("branchName",           sysPropOrArg("branchName",           args, 6,  "main"));
            jobBuildTag                 = checkIfEmpty("jobBuildTag",          sysPropOrArg("jobBuildTag",          args, 7,  "Build-1"));
            String sendEmailTo          = checkIfEmpty("sendEmailTo",          sysPropOrArg("sendEmailTo",          args, 8,  null));
            String sendSlackMessage     = checkIfEmpty("sendSlackMessage",     sysPropOrArg("sendSlackMessage",     args, 9,  "false"));
            isDebugMode                 = Boolean.parseBoolean(checkIfEmpty("debugMode",   sysPropOrArg("debugMode",              args, 10, "false")));
            boolean uploadToTestrail    = Boolean.parseBoolean(checkIfEmpty("uploadToTestrail",   sysPropOrArg("uploadToTestrail",       args, 11, "false")));
            boolean updateAutoStatus    = Boolean.parseBoolean(checkIfEmpty("updateAutomationStatus", sysPropOrArg("updateAutomationStatus", args, 12, "false")));
            isBrowserStackExecution     = Boolean.parseBoolean(checkIfEmpty("isBrowserStackExecution", sysPropOrArg("isBrowserStackExecution", args, 13, "false")));
            isRemoteExecution           = Boolean.parseBoolean(checkIfEmpty("isRemoteExecution",  sysPropOrArg("isRemoteExecution",      args, 14, "false")));

            boolean sendReportOnSlack = Boolean.parseBoolean(sendSlackMessage);

            System.out.println("=== Jarvis Test Runner ===");
            System.out.println("Project      : " + projectName);
            System.out.println("Environment  : " + environment);
            System.out.println("Browser      : " + browserName);
            System.out.println("Groups       : " + groupNames);
            System.out.println("Country      : " + country);
            System.out.println("Language     : " + appLanguage);
            System.out.println("Branch       : " + branchName);
            System.out.println("Build Tag    : " + jobBuildTag);
            System.out.println("Send Email   : " + sendEmailTo);
            System.out.println("Send Slack   : " + sendReportOnSlack);
            System.out.println("Debug        : " + isDebugMode);
            System.out.println("TestRail     : " + uploadToTestrail);
            System.out.println("BrowserStack : " + isBrowserStackExecution);
            System.out.println("Remote       : " + isRemoteExecution);
            System.out.println("===========================");

            // Propagate as system properties so Config picks them up
            System.setProperty("projectName",              projectName);
            System.setProperty("environment",              environment);
            System.setProperty("browserName",              browserName);
            System.setProperty("groups",                   groupNames);
            System.setProperty("groupName",                groupNames);
            System.setProperty("country",                  country.toLowerCase());
            System.setProperty("appLanguage",              appLanguage.toLowerCase());
            System.setProperty("branchName",               branchName);
            System.setProperty("debugMode",                String.valueOf(isDebugMode));
            System.setProperty("uploadToTestrail",         String.valueOf(uploadToTestrail));
            System.setProperty("isBrowserStackExecution",  String.valueOf(isBrowserStackExecution));
            System.setProperty("isRemoteExecution",        String.valueOf(isRemoteExecution));

            // Set ALL Config statics directly — @BeforeSuite will receive these via suite XML params,
            // but setting them here ensures they are available immediately and never null.
            Config.environment             = environment;
            Config.browserName             = browserName;
            Config.projectName             = projectName;
            Config.country                 = country;
            Config.appLanguage             = appLanguage;
            Config.groupName               = groupNames;
            Config.branchName              = branchName;
            Config.resultsDirectory        = resultsDirectory;
            Config.isDebugMode             = isDebugMode;
            Config.isBrowserStackExecution = isBrowserStackExecution;
            Config.isRemoteExecution       = isRemoteExecution;

            // Detect platform and thread count from group name
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

            // Decide results directory
            resultsDirectory = decideResultsDirectory(projectName, jobBuildTag);
            Config.resultsDirectory = resultsDirectory;
            System.setProperty("resultsDirectory", resultsDirectory);

            // Build, write and run the TestNG suite
            generateAndRunTestNGXml(projectName, environment, browserName, platform, groupNames,
                    country, appLanguage, branchName, threadCount, ParallelMode.TESTS);

            // Insert results into QA Dashboard database
            Config testConfig = new Config();
            addResultsToDatabase(testConfig, projectName, environment, groupNames, branchName);

            // Send Slack and email notifications
            triggerNotifications(testConfig, projectName, environment, sendReportOnSlack, sendEmailTo,
                    browserName, groupNames, branchName, platform);

            // After-suite actions: TestRail upload
            performAfterSuiteActions(testConfig, uploadToTestrail, updateAutoStatus);

            if (passPercentage != null && !passPercentage.equalsIgnoreCase("100"))
                System.exit(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            commentAndStopExecution("Exception occurred in GenerateTestngXmlAndRun.main");
        }

        System.exit(0);
    }

    // ============================================================================================
    // GENERATE AND RUN
    // ============================================================================================

    private static void generateAndRunTestNGXml(String projectName, String environment, String browserName,
            String platform, String groupNames, String country, String appLanguage,
            String branchName, int threadCount, ParallelMode parallelMode)
    {
        System.out.println("<----------------- CREATING TESTNG.XML -------------------->");

        XmlSuite suite = new XmlSuite();
        suite.setName(groupNames + " cases on " + branchName + " branch");
        suite.setParallel(parallelMode);
        suite.setThreadCount(threadCount);
        suite.setVerbose(0);

        // Suite-level parameters (read by TestBase / Config)
        suite.getParameters().put("projectName",           projectName);
        suite.getParameters().put("environment",           environment);
        suite.getParameters().put("browserName",            browserName);
        suite.getParameters().put("platform",              platform);
        suite.getParameters().put("country",               country.toLowerCase());
        suite.getParameters().put("appLanguage",           appLanguage.toLowerCase());
        suite.getParameters().put("groupName",             groupNames);
        suite.getParameters().put("branchName",            branchName);
        suite.getParameters().put("debugMode",             String.valueOf(isDebugMode));
        suite.getParameters().put("resultsDirectory",      resultsDirectory);
        suite.getParameters().put("remoteExecution",       String.valueOf(isRemoteExecution));
        suite.getParameters().put("browserStackExecution", String.valueOf(isBrowserStackExecution));

        // Discover test classes for the project
        List<String> testClasses = discoverTestClasses(projectName, platform);
        if (testClasses.isEmpty())
        {
            System.out.println("[WARN] No test classes found for project: " + projectName + ", platform: " + platform);
            System.out.println("[WARN] Add your test classes to GenerateTestngXmlAndRun.discoverTestClasses()");
        }

        // Parse include / exclude groups (prefix ~ to exclude, e.g. "regression,~apiCases")
        List<String> includeGroups = new ArrayList<>();
        List<String> excludeGroups = new ArrayList<>();
        for (String raw : groupNames.split(","))
        {
            String g = raw.trim();
            if (g.isEmpty()) continue;
            if (g.startsWith("~")) excludeGroups.add(g.substring(1));
            else includeGroups.add(g);
        }

        // One XmlTest node per included group for cleaner reporting (mirrors Thanos pattern)
        List<String> testsToCreate = includeGroups.isEmpty() ? List.of("all") : includeGroups;
        for (String group : testsToCreate)
        {
            XmlTest test = new XmlTest(suite);
            test.setName(group);
            test.setVerbose(0);
            if (!includeGroups.isEmpty()) test.addIncludedGroup(group);
            excludeGroups.forEach(test::addExcludedGroup);
            test.setXmlClasses(testClasses.stream().map(XmlClass::new).toList());
        }

        // Write XML to disk for reference / rerun
        String xmlPath = resultsDirectory + File.separator + "RunTime_TestNG.xml";
        writeXmlToDisk(suite, xmlPath);

        // Set ReportNG system properties
        System.setProperty("org.uncommons.reportng.title", projectName + " Test Report - " + environment);
        System.setProperty("org.uncommons.reportng.escape-output", "false");

        // Set ChainTest output paths to the same results directory as ReportNG
        System.setProperty("chaintest.generator.simple.output-file", resultsDirectory + File.separator + "chaintest" + File.separator + "Index.html");
        System.setProperty("chaintest.generator.email.output-file",  resultsDirectory + File.separator + "chaintest" + File.separator + "EmailReport.html");

        // Attach listeners
        TestNG testNG = new TestNG();
        testNG.setXmlSuites(List.of(suite));
        testNG.setOutputDirectory(resultsDirectory);
        testNG.setUseDefaultListeners(false);

        List<Class<? extends ITestNGListener>> listeners = new ArrayList<>();
        listeners.add(org.testng.reporters.FailedReporter.class);
        listeners.add(org.uncommons.reportng.HTMLReporter.class);
        listeners.add(org.uncommons.reportng.JUnitXMLReporter.class);
        listeners.add(com.aventstack.chaintest.plugins.ChainTestListener.class);
        testNG.setListenerClasses(listeners);

        System.out.println("<----------------- EXECUTING TESTNG.XML -------------------->");
        testNG.run();

        System.out.println("ChainTest Report: " + resultsDirectory + File.separator + "chaintest" + File.separator + "Index.html");
    }

    // ============================================================================================
    // DATABASE
    // ============================================================================================

    private static void addResultsToDatabase(Config testConfig, String projectName, String environment,
            String groupName, String branchName)
    {
        try
        {
            String htmlFolderPath = resultsDirectory + File.separator + "html";
            if (reportContents == null)
                reportContents = removeUnexecutedClassesAndFormatReport(htmlFolderPath);
            if (resultLink == null)
                resultLink = convertFilePathToUrl(htmlFolderPath + File.separator + "index.html");
            if (passPercentage == null)
            {
                Matcher matcher = Pattern.compile("class=\"passRate suite\">(\\d+)").matcher(reportContents);
                passPercentage = matcher.find() ? matcher.group(1) : "0";
            }

            long durationSeconds = (new Date().getTime() - startDate.getTime()) / 1000;
            String passedCases = "0";
            String failedCases = "0";
            Document document = Jsoup.parse(reportContents);
            for (org.jsoup.nodes.Element el : document.select("tr.suite>td.passed.number"))
                passedCases = el.text();
            for (org.jsoup.nodes.Element el : document.select("tr.suite>td.failed.number"))
                failedCases = el.text();
            String totalCases = String.valueOf(Integer.parseInt(passedCases) + Integer.parseInt(failedCases));

            String insertQuery = "INSERT INTO `automation_results`(`projectName`,`environment`,`groupName`,"
                    + "`duration`,`percentage`,`totalCases`,`passedCases`,`failedCases`,`buildTag`,`resultLink`) "
                    + "VALUES('" + projectName + "','" + environment.toLowerCase() + "','" + groupName + "','"
                    + durationSeconds + "','" + passPercentage + "','" + totalCases + "','" + passedCases + "','"
                    + failedCases + "','" + jobBuildTag + "','" + resultLink + "');";

            int count = (int) DatabaseHelper.executeQuery(testConfig, insertQuery, QueryType.update, DatabaseName.Automation);
            if (count == 1)
                System.out.println("Entry successfully created in Automation DB!");
            else
                System.out.println("Failed to create entry in Automation DB!");
        }
        catch (Exception e)
        {
            System.err.println("[WARN] DB insert skipped: " + e.getMessage());
        }
    }

    // ============================================================================================
    // NOTIFICATIONS
    // ============================================================================================

    private static void triggerNotifications(Config testConfig, String projectName, String environment,
            boolean sendReportOnSlack, String sendEmailTo, String browserName,
            String groupNames, String branchName, String platform)
    {
        try
        {
            String htmlFolderPath = resultsDirectory + File.separator + "html";

            // Populate shared state if not already done by addResultsToDatabase
            if (reportContents == null)
                reportContents = removeUnexecutedClassesAndFormatReport(htmlFolderPath);
            if (resultLink == null)
                resultLink = convertFilePathToUrl(htmlFolderPath + File.separator + "index.html");
            if (passPercentage == null)
            {
                Matcher matcher = Pattern.compile("class=\"passRate suite\">(\\d+)").matcher(reportContents);
                passPercentage = matcher.find() ? matcher.group(1) : "0";
            }

            // Compose subject / heading
            String headerEmoji  = passPercentage.equalsIgnoreCase("100") ? ":white_check_mark:" : ":fire:";
            String platformTag  = platform.equals("android") ? "[Android]"
                                : platform.equals("ios")     ? "[iOS]"
                                : "[" + browserName + "]";
            String countrySuffix = (Config.country != null && !Config.country.equalsIgnoreCase("ALL")
                                   && !Config.country.isEmpty())
                                   ? " - " + Config.country.toUpperCase() : "";

            String subject = passPercentage + "% : " + projectName + " test execution on "
                    + environment + countrySuffix + " " + platformTag;

            // Print report table to console
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(subject);
            printReportOnConsole(reportContents);
            System.out.println("Full Report: " + resultLink);
            System.out.println("=================================================================================");

            // Slack notification
            if (sendReportOnSlack)
            {
                String webhookUrl = testConfig.getRunTimeProperty("slack.webhook.url");
                if (StringUtils.isNotEmpty(webhookUrl))
                {
                    String slackMessage = "*" + headerEmoji + " QA AUTOMATION REPORT " + headerEmoji + "*\n"
                            + ":sparkles:-------------------------------------------------:sparkles:\n"
                            + ":line-arrow:  *" + passPercentage + "%* '" + groupNames
                            + "' tests passed for *'" + projectName + "'* on *" + environment + countrySuffix + "*\n"
                            + ":line-arrow:  *Build:* " + jobBuildTag + " | *Branch:* " + branchName + "\n"
                            + ":line-arrow:  Full Report: " + resultLink + "\n"
                            + "-----------------------------------------------------";
                    SlackHelper.sendMessage(webhookUrl, slackMessage);
                }
                else
                {
                    System.out.println("[WARN] slack.webhook.url not configured — Slack notification skipped.");
                }
            }

            // Email notification
            if (StringUtils.isNotEmpty(sendEmailTo))
            {
                EmailHelper.sendEmail(testConfig, sendEmailTo, subject, reportContents);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ============================================================================================
    // AFTER SUITE ACTIONS
    // ============================================================================================

    private static void performAfterSuiteActions(Config testConfig, boolean uploadToTestrail,
            boolean updateAutomationStatus)
    {
        if (uploadToTestrail)
        {
            System.out.println("[TestRail] Uploading results to TestRail...");
            // TODO: implement TestRailHelper.uploadResultToTestRun(TestBase.testResultObjects, resultLink)
        }
        if (updateAutomationStatus)
        {
            System.out.println("[TestRail] Updating automation status in TestRail...");
            // TODO: implement TestRailHelper.updateAutomationStatus(TestBase.testResultObjects)
        }
    }

    // ============================================================================================
    // REPORT HTML PROCESSING (mirrors Thanos — uses Jsoup + FileUtils)
    // ============================================================================================

    private static String removeUnexecutedClassesAndFormatReport(String htmlFolderPath)
    {
        String indexFile    = htmlFolderPath + File.separator + "index.html";
        String suitesFile   = htmlFolderPath + File.separator + "suites.html";
        String overviewFile = htmlFolderPath + File.separator + "overview.html";

        removeUnexecutedClasses(indexFile);
        String strText = removeUnexecutedClasses(overviewFile);
        if (strText == null) strText = "";

        strText = strText.replaceAll("output.html", resultLink != null ? resultLink : "output.html");
        if (isRemoteExecution)
        {
            strText = strText.replaceAll("href=\"suite", "href=\"" + convertFilePathToUrl(htmlFolderPath) + "/suite");
            strText = strText.replaceAll("Log Output", "Full Report");
        }
        else
        {
            strText = strText.replaceAll("href=\"suite", "href=\"#");
            strText = strText.replaceAll("target=\"_blank\"", "");
            strText = strText.replaceAll("Log Output", " ");
        }

        Matcher matcher = Pattern.compile("class=\"passRate suite\">(\\d+)").matcher(strText);
        if (matcher.find())
        {
            int pct = Integer.parseInt(matcher.group(1));
            if (pct < 100) formatReport(suitesFile);
        }
        else
        {
            System.err.println("[WARN] Unable to extract pass percentage from report HTML.");
        }

        return strText;
    }

    private static void formatReport(String filePath)
    {
        String predefinedFormat = "<!--?xml version=\"1.0\" encoding=\"utf-8\" ?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"\" lang=\"\">\n" + " <head>\n"
                + "  <title>Jarvis Test Execution Report</title>\n"
                + "  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n"
                + "  <link href=\"reportng.css\" rel=\"stylesheet\" type=\"text/css\">\n"
                + "  <script type=\"text/javascript\" src=\"reportng.js\"></script>\n"
                + "  <style type=\"text/css\">.test{font-size:1em;font-family:sans-serif;}</style>\n"
                + " </head>\n" + " <body style=\"margin-top: 0;\">\n"
                + "  <div id=\"sidebarHeader\"><h2>Jarvis Test Execution Report</h2>\n"
                + "   <p><a href=\"overview.html\" target=\"main\">Overview</a> · <a href=\"output.html\" target=\"main\">Log Output</a></p>\n"
                + "  </div>\n"
                + "  <table id=\"suites\"><thead><tr>"
                + "<th class=\"header suite\" onclick=\"toggleElement('tests-1','table-row-group');toggle('toggle-1')\" style=\"line-height:1.6em;white-space:nowrap;\">"
                + "<span id=\"toggle-1\" class=\"toggle\">&#x25bc;</span>List of testcases executed:</th></tr></thead>\n"
                + "  <tbody id=\"tests-1\" class=\"tests\"><tr><td>\n"
                + "   <table id=\"suites\" style=\"padding-left:12px;\"><thead><tr>"
                + "<th class=\"suite\" onclick=\"toggleElement('tests-failures','table-row-group');toggle('toggle-failures')\" style=\"text-align:left;line-height:1.5em;background-color:#ff8888;\">"
                + "<span id=\"toggle-failures\" class=\"toggle\">&#x25bc;</span>Failed Cases</th></tr></thead>\n"
                + "   <tbody id=\"tests-failures\" class=\"tests\"></tbody></table></td></tr><tr><td>\n"
                + "   <table id=\"suites\" style=\"padding-left:12px;\"><thead><tr>"
                + "<th class=\"suite\" onclick=\"toggleElement('tests-passed','table-row-group');toggle('toggle-passed')\" style=\"text-align:left;line-height:1.5em;background-color:#88ee88;\">"
                + "<span id=\"toggle-passed\" class=\"toggle\">&#x25b6;</span>Passed Cases</th></tr></thead>\n"
                + "   <tbody id=\"tests-passed\" class=\"tests\" style=\"display:none;\"></tbody></table></td></tr>\n"
                + "  </tbody></table>\n"
                + " </body>\n</html>";
        try
        {
            File file = new File(normalizePath(filePath));
            if (!file.exists()) return;
            String strFileData = FileUtils.readFileToString(file, "UTF8");
            Document doc1 = Jsoup.parse(strFileData);
            Document doc2 = Jsoup.parse(predefinedFormat);
            org.jsoup.nodes.Element testsFailures = doc2.getElementById("tests-failures");
            org.jsoup.nodes.Element testsPassed   = doc2.getElementById("tests-passed");
            for (org.jsoup.nodes.Element el : doc1.getElementsByClass("failureIndicator"))
            {
                el = el.parent().parent();
                testsFailures.append(el.html().replace("?", "&#x2718;"));
                el.remove();
            }
            for (org.jsoup.nodes.Element el : doc1.getElementsByClass("successIndicator"))
            {
                el = el.parent().parent();
                testsPassed.append(el.html().replace("?", "&#x2714;"));
                el.remove();
            }
            FileUtils.writeStringToFile(file, doc2.toString(), "UTF8");
        }
        catch (Exception e)
        {
            System.out.println("Exception while reformatting TestNG report: " + e.getMessage());
        }
    }

    private static String removeUnexecutedClasses(String filePath)
    {
        String strFileData = null;
        File file = new File(normalizePath(filePath));
        if (!file.exists()) return "";
        try
        {
            strFileData = FileUtils.readFileToString(file, "UTF8");
            strFileData = strFileData.replace("<frameset cols=\"20%,*\">", "<frameset cols=\"24%,*\">");
            Document document = Jsoup.parse(strFileData);
            for (org.jsoup.nodes.Element element : document.select("td:eq(5)"))
            {
                String content = element.getElementsMatchingOwnText("N/A").text();
                if (content.equalsIgnoreCase("N/A"))
                {
                    element = element.parent();
                    element.remove();
                }
            }
            strFileData = document.toString();
            FileUtils.writeStringToFile(file, strFileData, "UTF8");
        }
        catch (Exception e)
        {
            System.out.println("Exception while removing N/A rows from report: " + e.getMessage());
        }
        return strFileData;
    }

    // ============================================================================================
    // CONSOLE REPORT PRINTING (mirrors Thanos — parses ReportNG overview.html)
    // ============================================================================================

    private static void printReportOnConsole(String strText)
    {
        try
        {
            Document doc = Jsoup.parse(strText);
            Elements rows = doc.body().getElementsByTag("tr");
            List<List<String>> data = new ArrayList<>();
            int max = -1;

            for (org.jsoup.nodes.Element row : rows)
            {
                List<String> rowData = new ArrayList<>();
                int j = 0;
                Elements colHead = row.getElementsByTag("th");
                for (org.jsoup.nodes.Element col : colHead)
                {
                    rowData.add(j, col.text());
                    if (!StringUtils.isEmpty(rowData.get(j)) && max < rowData.get(j).length())
                        max = rowData.get(j).length();
                    j++;
                }
                j = 0;
                Elements cols = row.getElementsByTag("td");
                for (org.jsoup.nodes.Element col : cols)
                {
                    rowData.add(j, col.text());
                    if (!StringUtils.isEmpty(rowData.get(j)) && max < rowData.get(j).length())
                        max = rowData.get(j).length();
                    j++;
                }
                if (!rowData.isEmpty())
                {
                    if (!rowData.get(0).equals("Total"))
                    {
                        if (rowData.size() > 1) rowData.remove(1);
                        if (rowData.size() > 2) rowData.remove(2);
                    }
                    else
                    {
                        if (rowData.size() > 2) rowData.remove(2);
                    }
                    data.add(rowData);
                }
            }

            int newMax = max;
            for (int i = 0; i < (max + 42); i++) System.out.print("=");
            System.out.println();
            for (int i = 1; i < data.size(); i++)
            {
                System.out.print("| ");
                max = newMax;
                for (int k = 0; k < 4; k++)
                {
                    String dataToPrint = "";
                    int len = 0;
                    try { dataToPrint = data.get(i).get(k); System.out.print(dataToPrint); len = dataToPrint.length(); }
                    catch (Exception e) { System.out.print(""); }
                    for (int j = 0; j < max - len; j++) System.out.print(" ");
                    System.out.print(" | ");
                    max = 10;
                }
                System.out.println();
            }
            for (int i = 0; i < (newMax + 42); i++) System.out.print("=");
            System.out.println();
        }
        catch (Exception e)
        {
            System.out.println("[WARN] Could not print report table: " + e.getMessage());
        }
    }

    // ============================================================================================
    // RESULTS DIRECTORY
    // ============================================================================================

    private static String decideResultsDirectory(String projectName, String jobBuildTag)
    {
        String baseDir = normalizePath(System.getProperty("user.dir") + File.separator + "test-output");
        String dir;
        if (isRemoteExecution)
        {
            // Remote / CI: isolate each build under its own jobBuildTag folder
            dir = normalizePath(baseDir + File.separator + projectName + File.separator
                    + jobBuildTag.replace("jenkins-", ""));
        }
        else
        {
            // Local: everything goes straight into test-output/
            dir = baseDir;
        }
        if (!new File(dir).mkdirs() && !new File(dir).exists())
        {
            System.err.println("[WARN] Could not create results directory: " + dir + ". Falling back to test-output.");
            dir = baseDir;
            new File(dir).mkdirs();
        }
        System.out.println("Results directory: " + dir);
        return dir;
    }

    // ============================================================================================
    // TEST CLASS DISCOVERY
    // ============================================================================================

    /**
     * Returns fully-qualified test class names to include for a given project.
     * Add new project mappings here.
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
            case "fullsuite" ->
            {
                classes.add("automation.github.GitHubApiTest");
                classes.add("automation.github.GitHubLoginTest");
                classes.add("automation.saucedemo.SauceDemoApiTest");
                classes.add("automation.saucedemo.SauceDemoWebTest");
            }
        }

        // Filter by platform
        if ("api".equals(platform))
            classes = classes.stream().filter(c -> c.contains("Api")).toList();
        else if ("android".equals(platform) || "ios".equals(platform))
            classes = classes.stream().filter(c -> c.contains("Mobile") || c.contains("App")).toList();

        return classes;
    }

    // ============================================================================================
    // UTILITIES
    // ============================================================================================

    private static void writeXmlToDisk(XmlSuite suite, String path)
    {
        try
        {
            new File(path).getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(new File(path)))
            {
                fw.write(suite.toXml());
                System.out.println("TestNG XML written to: " + path);
                logCommentForDebugging("Created XML:\n" + suite.toXml());
            }
        }
        catch (IOException e)
        {
            System.err.println("Failed to write TestNG XML: " + e.getMessage());
        }
    }

    /**
     * Converts a local filesystem path to a file:// URL (equivalent of Thanos CommonUtilities.convertFilePathToHtmlUrl).
     */
    private static String convertFilePathToUrl(String path)
    {
        try
        {
            return new File(normalizePath(path)).toURI().toURL().toString();
        }
        catch (Exception e)
        {
            return "file://" + path.replace("\\", "/");
        }
    }

    /**
     * Normalizes path separators for the current OS (equivalent of Thanos CommonUtilities.normalizePath).
     */
    private static String normalizePath(String path)
    {
        return Paths.get(path).normalize().toString();
    }

    private static void commentAndStopExecution(String message)
    {
        String colored = "\033[31m =======>>" + message + "<<======= \033[0m";
        System.out.println("\n" + colored + "\n");
        System.exit(1);
    }

    private static void logCommentForDebugging(String message)
    {
        if (isDebugMode)
            System.out.println(message);
    }

    /**
     * Checks if a value is empty or null and returns the default (mirrors Thanos checkIfEmpty).
     * Also prints the argument with its index for traceability.
     */
    private static String checkIfEmpty(String argumentName, String value)
    {
        String finalValue = (StringUtils.isEmpty(value) || "null".equalsIgnoreCase(value)) ? null : value.trim();
        System.out.println("Value of argument[" + variableCount + "]-" + argumentName + " = " + finalValue);
        variableCount++;
        return finalValue;
    }

    /**
     * Returns the value of a named system property if set, otherwise falls back to the positional
     * CLI arg at {@code index}, otherwise returns {@code defaultValue}.
     */
    private static String sysPropOrArg(String propertyName, String[] args, int index, String defaultValue)
    {
        String sysProp = System.getProperty(propertyName);
        if (sysProp != null && !sysProp.isEmpty()) return sysProp;
        if (args.length > index && args[index] != null && !args[index].isEmpty()) return args[index];
        return defaultValue;
    }
}
