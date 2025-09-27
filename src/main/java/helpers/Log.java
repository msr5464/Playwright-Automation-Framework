package helpers;

import org.testng.Assert;
import org.testng.Reporter;

class Log {

	public static void commentJson(Config config, String message, String color) {
		logToStandard(config, message);
		message = message.replaceAll("\n", "</br>").replaceAll(" ", "&nbsp");
		message = "<font color='" + color + "'>" + message + "</font></br>";
		logInReporter(message);
		config.testLog = config.testLog.concat(message);
	}

	public static void comment(Config config, String message, String color) {
		logToStandard(config, message);
		message = "<font color='" + color + "'>" + message + "</font></br>";
		logInReporter(message);
		config.testLog = config.testLog.concat(message);
	}

	public static void comment(Config config, String message) {
		comment(config, message, "Black");
	}

	public static void Fail(Config config, String message) {
		failure(config, message);
		BrowserHelper.takeScreenshot(config);
	}

	public static void failure(Config config, String message) {
		String tempMessage = message;
		config.softAssert.fail(message);
		logToStandard(config, message);
		message = "<font color='Red'>" + message + "</font></br>";
		logInReporter(message);
		config.testLog = config.testLog.concat(message);
		// Stop the execution if end execution flag is ON
		if (config.endExecutionOnfailure)
			Assert.fail("==>" + tempMessage);
	}

	private static void logToStandard(Config config, String message) {
		System.out.println(message);
	}

	public static void logInReporter(String message) {
		Reporter.log(message);
	}

	public static void pass(Config config, String message) {
		logToStandard(config, message);
		message = "<font color='Green'>" + message + "</font></br>";
		logInReporter(message);
		config.testLog = config.testLog.concat(message);
	}

	public static void warning(Config config, String message) {
		logToStandard(config, message);
		message = "<font color='Orange'>" + message + "</font></br>";
		logInReporter(message);
		config.testLog = config.testLog.concat(message);
	}

	public static void warning(Config config, String message, boolean takeScreenshot) {
		if (takeScreenshot)
			BrowserHelper.takeScreenshot(config);
		warning(config, message);
	}

}