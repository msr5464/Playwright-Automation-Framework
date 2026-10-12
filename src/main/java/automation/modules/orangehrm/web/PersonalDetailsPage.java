package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.orangehrm.OrangeHRMData;
import automation.modules.orangehrm.OrangeHRMEnums.Gender;

public class PersonalDetailsPage extends BasePage
{
    private final Locator employeeNameHeader;
    private final Locator employeeIdField;
    private final Locator nationalityDropdown;
    private final Locator maritalStatusDropdown;
    private final Locator dateOfBirthField;
    private final Locator savePersonalDetailsButton;
    private final Locator toastMessage;
    private final Locator employeeListTabLink;

    public PersonalDetailsPage(Config config)
    {
        super(config);
        employeeNameHeader        = page.locator("div.orangehrm-edit-employee-name h6");
        employeeIdField           = page.locator("div.oxd-input-group:has(label:text-is('Employee Id')) input");
        nationalityDropdown       = page.locator("div.oxd-input-group:has(label:has-text('Nationality')) div.oxd-select-text");
        maritalStatusDropdown     = page.locator("div.oxd-input-group:has(label:has-text('Marital Status')) .oxd-select-text");
        dateOfBirthField          = page.locator("div.oxd-input-group:has(label:has-text('Date of Birth')) input");
        savePersonalDetailsButton = page.locator("form:has(div.oxd-input-group:has(label:has-text('Nationality'))) button[type='submit']");
        toastMessage              = page.locator("p.oxd-text.oxd-text--p.oxd-text--toast-message.oxd-toast-content-text");
        employeeListTabLink       = page.locator("a:has-text('Employee List')");
        assertPageLoaded(employeeNameHeader);
    }

    public String getEmployeeNameHeader()
    {
        return getText(employeeNameHeader, "Employee name header");
    }

    /**
     * Returns the Employee Id as persisted and shown on the Personal Details page itself,
     * read right after Save lands here - this is the Id actually committed, unlike the
     * value the Add Employee page shows beforehand, which is only a pre-save suggestion.
     */
    public String getEmployeeId()
    {
        return getInputValue(employeeIdField, "Employee Id field");
    }

    /**
     * Fills Nationality, Marital Status, Date of Birth and Gender in one call, since these
     * small field actions on the same page belong together as a single higher-level action.
     */
    public PersonalDetailsPage fillPersonalDetails(OrangeHRMData employee)
    {
        Log.comment(config, "Filling personal details for employee");
        selectFromCustomDropdown(nationalityDropdown, employee.getNationality(), "Nationality dropdown");
        selectFromCustomDropdown(maritalStatusDropdown, employee.getMaritalStatus(), "Marital Status dropdown");
        fillText(dateOfBirthField, employee.getDateOfBirth(), "Date of Birth field");
        dismissOverlayIfVisible(page.locator("div.oxd-date-input--dropdown"));
        selectGender(Gender.valueOf(employee.getGender()));
        return this;
    }

    private void selectFromCustomDropdown(Locator dropdown, String value, String fieldName)
    {
        click(dropdown, fieldName);
        Locator option = page.locator("div[role='option'] span:text-is('" + value + "')");
        WaitHelper.waitForElementToBeVisible(config, option, fieldName + " option: " + value);
        click(option, fieldName + " option: " + value);
    }

    /**
     * Selects the given Gender option. Its locator is the confirmed selector with only the
     * key replaced, so every option is selectable through the same code. Clicking once can
     * sometimes only dismiss an open date-picker calendar instead of selecting the option, so
     * a second click is issued if the calendar is still visible afterwards.
     */
    public PersonalDetailsPage selectGender(Gender gender)
    {
        Locator genderLabel = page.locator("label:text-is('" + gender.getKey() + "')");
        click(genderLabel, gender.getLabel() + " gender option");
        if (isElementDisplayed(page.locator("div.oxd-date-input--dropdown")))
        {
            click(genderLabel, gender.getLabel() + " gender option (retry)");
        }
        return this;
    }

    public PersonalDetailsPage savePersonalDetails()
    {
        Log.comment(config, "Saving personal details");
        click(savePersonalDetailsButton, "Save button (Personal Details)");
        WaitHelper.waitForElementToBeVisible(config, toastMessage, "Toast message");
        return this;
    }

    public String getToastMessage()
    {
        return getText(toastMessage, "Toast message");
    }

    public String getNationality()
    {
        return getText(nationalityDropdown, "Nationality dropdown");
    }

    public String getMaritalStatus()
    {
        return getText(maritalStatusDropdown, "Marital Status dropdown");
    }

    public String getDateOfBirth()
    {
        return getInputValue(dateOfBirthField, "Date of Birth field");
    }

    /**
     * Returns the currently selected gender's label, read from whichever gender radio is
     * checked. Only Gender.Male has been exercised/automated so far.
     */
    public String getGender()
    {
        Locator genderRow = page.locator("div.oxd-input-group:has(label:has-text('Gender'))");
        Locator maleRadio = genderRow.locator("input[type='radio']").first();
        return isElementChecked(maleRadio) ? Gender.Male.getLabel() : "";
    }

    public EmployeeListPage goToEmployeeListTab()
    {
        Log.comment(config, "Navigating to Employee List tab");
        click(employeeListTabLink, "Employee List tab");
        return new EmployeeListPage(config);
    }
}
