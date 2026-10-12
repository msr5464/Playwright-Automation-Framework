package automation.modules.orangehrm;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.api.ApiHelper;
import automation.modules.orangehrm.web.AddEmployeePage;
import automation.modules.orangehrm.web.DashboardPage;
import automation.modules.orangehrm.web.DeleteConfirmationDialog;
import automation.modules.orangehrm.web.EmployeeListPage;
import automation.modules.orangehrm.web.LoginPage;
import automation.modules.orangehrm.web.PersonalDetailsPage;

/**
 * Helper for OrangeHRM web flows. Holds one public field per page of the module - every
 * page action that leaves a page returns the next page object, and the test stores it on
 * that page's field, so each step continues from the page the previous step returned.
 *
 * Usage - the Helper holds the pages, the test chains through them:
 *   OrangeHRMHelper orangehrm = new OrangeHRMHelper(config);
 *   orangehrm.dashboardPage = orangehrm.login();
 *   orangehrm.personalDetailsPage = orangehrm.addEmployee(employee);
 *   orangehrm.employeeListPage = orangehrm.deleteEmployee(employeeId);
 */
public class OrangeHRMHelper extends ApiHelper
{
    public LoginPage loginPage;
    public DashboardPage dashboardPage;
    public EmployeeListPage employeeListPage;
    public AddEmployeePage addEmployeePage;
    public PersonalDetailsPage personalDetailsPage;
    public DeleteConfirmationDialog deleteConfirmationDialog;

    public OrangeHRMHelper(Config config)
    {
        super(config, config.getRunTimeProperty("orangehrm.url"));
    }

    /**
     * Entry operation: navigates to OrangeHRM and logs in as Admin. The only place a page
     * object is constructed, right after navigating. Returns the Dashboard page it lands on.
     */
    public DashboardPage login()
    {
        String url = config.getRunTimeProperty("orangehrm.url");
        Log.comment(config, "Navigating to OrangeHRM: " + url);
        BrowserHelper.navigateTo(config, url);
        loginPage = new LoginPage(config);
        dashboardPage = loginPage.login("Admin", "admin123");
        return dashboardPage;
    }

    /**
     * Composed operation: continues from the Dashboard page already in its field, navigates
     * to PIM, opens the Add Employee tab, fills the employee's name and saves. Returns the
     * Personal Details page it lands on.
     */
    public PersonalDetailsPage addEmployee(OrangeHRMData employee)
    {
        employeeListPage = dashboardPage.navigateToPim();
        addEmployeePage = employeeListPage.goToAddEmployeeTab();
        addEmployeePage.fillEmployeeName(employee);
        personalDetailsPage = addEmployeePage.save();
        return personalDetailsPage;
    }

    /**
     * Composed operation: continues from the Employee List page already in its field,
     * searches by Employee Id, clicks the trash icon and confirms deletion. Returns the
     * Employee List page it lands on.
     */
    public EmployeeListPage deleteEmployee(String employeeId)
    {
        employeeListPage = employeeListPage.searchByEmployeeId(employeeId);
        deleteConfirmationDialog = employeeListPage.clickDeleteOnRow();
        employeeListPage = deleteConfirmationDialog.confirmDelete();
        return employeeListPage;
    }
}
