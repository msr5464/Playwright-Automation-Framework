package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.modules.orangehrm.OrangeHRMData;

public class AddEmployeePage extends BasePage
{
    private final Locator firstNameField;
    private final Locator middleNameField;
    private final Locator lastNameField;
    private final Locator employeeIdField;
    private final Locator saveButton;

    public AddEmployeePage(Config config)
    {
        super(config);
        firstNameField  = page.locator("input[name='firstName']");
        middleNameField = page.locator("input[name='middleName']");
        lastNameField    = page.locator("input[name='lastName']");
        employeeIdField  = page.locator("div.oxd-grid-2.orangehrm-full-width-grid input.oxd-input.oxd-input--active");
        saveButton       = page.locator("button[type='submit']");
        assertPageLoaded(firstNameField);
    }

    public AddEmployeePage fillEmployeeName(OrangeHRMData employee)
    {
        Log.comment(config, "Filling employee name: " + employee.getFirstName() + " " + employee.getMiddleName() + " " + employee.getLastName());
        fillText(firstNameField, employee.getFirstName(), "First name field");
        fillText(middleNameField, employee.getMiddleName(), "Middle name field");
        fillText(lastNameField, employee.getLastName(), "Last name field");
        return this;
    }

    public String getEmployeeId()
    {
        return getInputValue(employeeIdField, "Employee Id field");
    }

    public PersonalDetailsPage save()
    {
        Log.comment(config, "Saving new employee");
        click(saveButton, "Save button");
        return new PersonalDetailsPage(config);
    }
}
