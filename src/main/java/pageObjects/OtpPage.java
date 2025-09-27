package pageObjects;

import com.microsoft.playwright.Locator;

import helpers.Config;
import helpers.Element;

public class OtpPage {

	private Config config;
	private Locator otpInputField;
	private Locator verifyButton;

	public OtpPage(Config config) {
		this.config = config;
		this.otpInputField = config.page.locator("input[name='otp']");
		this.verifyButton = config.page.locator("button[type='submit']");
	}

	public void enterOtp(String otp) {
		Element.enterData(config, otpInputField, otp, "OTP Input Field");
	}

	public DashboardPage clickVerifyButton() {
		Element.click(config, verifyButton, "Verify Button");
		return new DashboardPage(config);
	}
}
