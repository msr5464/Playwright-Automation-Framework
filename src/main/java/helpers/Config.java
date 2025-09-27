package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.asserts.SoftAssert;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Config {
	public static String environment;
	public static String browserName;
	public static String resultsDirectory;
	public static boolean isRemoteExecution = true;
	public static boolean isDebugMode = false;
	public static String osName = System.getProperty("os.name");
	static HashMap<String, TestDataReader> testDataReaderHashMap = new HashMap<String, TestDataReader>();

	public boolean endExecutionOnfailure = false;
	String testLog = "";
	public String testcaseName;
	public String testcaseClass;

	SoftAssert softAssert = null;
	Properties runTimeProperties = null;
	int testcasesRemaining = 0;
	boolean enableScreenshot = true;
	boolean testResult = true;
	boolean retry = true;

	public static Playwright playwright;
	public static Browser browser;
	public BrowserContext browserContext;
	public Page page;
	public HashMap<String, String> testData;
	public String mainResourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main"
			+ File.separator + "resources" + File.separator;
	public String testResourcesPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator;

	public Config() {
		softAssert = new SoftAssert();
		runTimeProperties = new Properties();
		Properties properties = null;
		// Code to read .properties file and put key value pairs into RunTime Property
		// file
		try {
			FileInputStream fileInputStream = new FileInputStream(mainResourcesPath + "config.properties");
			properties = new Properties();
			properties.load(fileInputStream);
			fileInputStream.close();

			// override the environment value if passed through TestNG.xml
			if (!StringUtils.isEmpty(environment))
				properties.put("Environment", environment.toLowerCase());
			fileInputStream = new FileInputStream(mainResourcesPath + properties.get("Environment") + ".properties");
			logComment("Running on '" + properties.get("Environment") + "' environment");
			properties.load(fileInputStream);
			fileInputStream.close();
		} catch (Exception e) {
			logComment("Exception while reading config.properties file...");
			e.printStackTrace();
		}

		Enumeration<Object> enumeration = properties.keys();
		while (enumeration.hasMoreElements()) {
			String str = (String) enumeration.nextElement();
			putRunTimeProperty(str, (String) properties.get(str));
		}

		// override param values if passed through TestNG.xml
		if (!StringUtils.isEmpty(resultsDirectory)) {
			putRunTimeProperty("ResultsDirectory", resultsDirectory);
		} else {
			resultsDirectory = System.getProperty("user.dir") + File.separator + "test-output";
			putRunTimeProperty("ResultsDirectory", resultsDirectory);
		}
		if (!StringUtils.isEmpty(browserName))
			putRunTimeProperty("Browser", browserName);

		// Putting values into variables from RunTime properties
		endExecutionOnfailure = endExecutionOnfailure
				|| getRunTimeProperty("EndExecutionOnFailure").equalsIgnoreCase("true");
		isRemoteExecution = isRemoteExecution || getRunTimeProperty("RemoteExecution").equalsIgnoreCase("true");
		isDebugMode = isDebugMode || getRunTimeProperty("debugMode").equalsIgnoreCase("true");
		environment = getRunTimeProperty("Environment");
	}

	public void putRunTimeProperty(String key, String value) {
		if (isDebugMode)
			logComment("Putting RunTime key-" + key.toLowerCase() + " value:-'" + value + "'");
		runTimeProperties.put(key.toLowerCase(), value);
	}

	public void putRunTimeProperty(String key, Object value) {
		String keyName = key.toLowerCase();
		runTimeProperties.put(keyName, value);
		if (isDebugMode)
			logComment("Putting Run-Time key-" + keyName + " value:-'" + value + "'");
	}

	public String getRunTimeProperty(String key) {
		String keyName = key.toLowerCase();
		String value = "";
		try {
			value = runTimeProperties.get(keyName).toString();
			if (isDebugMode)
				logComment("Read RunTime Property -'" + keyName + "' as -'" + value + "'");
		} catch (Exception e) {
			if (isDebugMode)
				logComment("'" + key + "' not found in Run Time Properties");
			return null;
		}
		return value;
	}

	public String replaceArgumentsWithRunTimeProperties(String input) {
		if (input.contains("{$")) {
			int index = input.indexOf("{$");
			input.length();
			input.indexOf("}", index + 2);
			String key = input.substring(index + 2, input.indexOf("}", index + 2));
			String value = getRunTimeProperty(key);
			input = input.replace("{$" + key + "}", value);
			return replaceArgumentsWithRunTimeProperties(input);
		}
		return input;
	}

	public void logComment(String message) {
		Log.comment(this, message);
	}

	public void logCommentJson(String message, String color) {
		Log.commentJson(this, message, color);
	}

	public void logColorfulComment(String message, String color) {
		Log.comment(this, message, color);
	}

	public void logCommentForDebugging(String message) {
		if (isDebugMode)
			Log.comment(this, message);
	}

	public void logWarning(String message) {
		Log.warning(this, message);
	}

	public void logWarning(String message, boolean pageCapture) {
		Log.warning(this, message, pageCapture);
	}

	public void logWarning(String what, String expected, String actual) {
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.warning(this, message);
	}

	public void logFail(String message) {
		testResult = false;
		Log.Fail(this, message);
	}

	public void logFailToEndExecution(String message) {
		BrowserHelper.takeScreenshot(this);
		enableScreenshot = false;
		retry = false;
		endExecutionOnfailure = true;
		testResult = false;
		Log.Fail(this, message);
	}

	public <T> void logFail(String what, T expected, T actual) {
		testResult = false;
		String message = "Expected '" + what + "' was :-'" + expected + "'. But actual is '" + actual + "'";
		Log.Fail(this, message);
	}

	public void logPass(String message) {
		Log.pass(this, message);
	}

	public <T> void logPass(String what, T actual) {
		String message = "Verified '" + what + "' as :-'" + actual + "'";
		Log.pass(this, message);
	}

	public void logExceptionAndFail(Throwable e) {
		logExceptionAndFail("", e);
	}

	public void logExceptionAndFail(String message, Throwable e) {
		testResult = false;
		String fullStackTrace = ExceptionUtils.getStackTrace(e);
		Log.Fail(this, message + "\nException Message:- " + fullStackTrace);
	}

	public void logException(String message, Throwable e) {
		if (e.getMessage() == null) {
			logWarning(message);
			if (isDebugMode) {
				String fullStackTrace = ExceptionUtils.getStackTrace(e);
				Log.warning(this, " \nFull Exception Stacktrace:- \n" + fullStackTrace);
			}
		} else {
			logWarning(message + ". \nException Message:- " + e.getMessage());
		}
		if (isDebugMode) {
			String fullStackTrace = ExceptionUtils.getStackTrace(e);
			Log.warning(this, " \nFull Exception Stacktrace:- \n" + fullStackTrace);
		}
	}

	public TestDataReader getCsvFile(String sheetName) {
		TestDataReader testDataReader = null;
		String csvFile = testResourcesPath + "CsvFiles" + File.separator + sheetName + ".csv";

		synchronized (Config.class) {
			testDataReader = testDataReaderHashMap.get(csvFile);
			if (testDataReader == null) {
				testDataReader = new TestDataReader(this, sheetName, csvFile);
				testDataReaderHashMap.put(csvFile + sheetName, testDataReader);
			}
		}
		return testDataReader;
	}
}