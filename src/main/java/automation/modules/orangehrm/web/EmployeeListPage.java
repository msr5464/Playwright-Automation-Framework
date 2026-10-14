package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;

public class EmployeeListPage extends BasePage
{
    private final Locator addEmployeeTab;
    private final Locator employeeIdFilterField;
    private final Locator searchButton;
    private final Locator recordFoundText;
    private final Locator employeeIdCell;
    private final Locator employeeNameCell;
    private final Locator employeeLastNameCell;
    private final Locator editIcon;
    private final Locator deleteIcon;
    private final Locator rowCheckboxLabel;
    private final Locator deleteSelectedButton;
    private final Locator toastMessage;
    private final Locator userDropdownMenu;
    private final Locator logoutLink;

    public EmployeeListPage(Config config)
    {
        super(config);
        addEmployeeTab        = page.locator("a:has-text('Add Employee')");
        employeeIdFilterField = page.locator("div.oxd-input-group:has(label:text-is('Employee Id')) input");
        searchButton          = page.locator("button[type='submit']");
        recordFoundText       = page.locator("span:has-text('Record')");
        employeeIdCell         = page.locator("div.oxd-table-row:nth-child(1) div.oxd-table-cell:nth-child(2)");
        employeeNameCell       = page.locator("div.oxd-table-row:nth-child(1) div.oxd-table-cell:nth-child(3)");
        employeeLastNameCell   = page.locator("div.oxd-table-row:nth-child(1) div.oxd-table-cell:nth-child(4)");
        editIcon               = page.locator("div.oxd-table-row:nth-child(1) button:has(i.bi-pencil-fill)");
        deleteIcon             = page.locator("div.oxd-table-row:nth-child(1) button:has(i.bi-trash)");
        rowCheckboxLabel       = page.locator("div.oxd-table-card-cell-checkbox label");
        deleteSelectedButton   = page.locator("button:has-text('Delete Selected')");
        toastMessage           = page.locator("p.oxd-text.oxd-text--p.oxd-text--toast-message.oxd-toast-content-text");
        userDropdownMenu       = page.locator("span.oxd-userdropdown-tab");
        logoutLink             = page.locator("a[href='/web/index.php/auth/logout']");
        assertPageLoaded(addEmployeeTab);
    }

    public AddEmployeePage goToAddEmployeeTab()
    {
        Log.comment(config, "Navigating to Add Employee tab");
        click(addEmployeeTab, "Add Employee tab");
        return new AddEmployeePage(config);
    }

    public EmployeeListPage searchByEmployeeId(String employeeId)
    {
        Log.comment(config, "Searching for employee by Id: " + employeeId);
        fillText(employeeIdFilterField, employeeId, "Employee Id filter field");
        click(searchButton, "Search button");
        WaitHelper.waitForPageToSettle(config);
        return this;
    }

    public String getRecordFoundText()
    {
        return getText(recordFoundText, "Record found text");
    }

    public String getEmployeeIdInRow()
    {
        return getText(employeeIdCell, "Employee Id cell");
    }

    public String getFirstAndMiddleNameInRow()
    {
        return getText(employeeNameCell, "Employee first & middle name cell");
    }

    public String getLastNameInRow()
    {
        return getText(employeeLastNameCell, "Employee last name cell");
    }

    public PersonalDetailsPage clickEditOnRow()
    {
        Log.comment(config, "Clicking edit icon on employee row");
        click(editIcon, "Edit icon");
        return new PersonalDetailsPage(config);
    }

    public DeleteConfirmationDialog clickDeleteOnRow()
    {
        Log.comment(config, "Clicking delete icon on employee row");
        click(deleteIcon, "Delete icon");
        return new DeleteConfirmationDialog(config);
    }

    public EmployeeListPage checkRowCheckbox()
    {
        Log.comment(config, "Ticking checkbox on employee row");
        click(rowCheckboxLabel, "Row checkbox");
        return this;
    }

    public DeleteConfirmationDialog clickDeleteSelected()
    {
        Log.comment(config, "Clicking Delete Selected button");
        click(deleteSelectedButton, "Delete Selected button");
        return new DeleteConfirmationDialog(config);
    }

    public String getToastMessage()
    {
        WaitHelper.waitForElementToBeVisible(config, toastMessage, "Toast message");
        return getText(toastMessage, "Toast message");
    }

    public LoginPage logout()
    {
        Log.comment(config, "Logging out of OrangeHRM");
        click(userDropdownMenu, "User dropdown menu");
        WaitHelper.waitForElementToBeVisible(config, logoutLink, "Logout link");
        click(logoutLink, "Logout link");
        return new LoginPage(config);
    }
}
