package automation.orangehrm;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.orangehrm.OrangeHRMBuilder;
import automation.modules.orangehrm.OrangeHRMData;
import automation.modules.orangehrm.OrangeHRMHelper;

public class OrangeHRMWebTest extends TestBase
{

    @Test(description = "Add a new employee, verify the Employee List search finds it, verify Personal Details persisted, delete the employee and verify it is gone, then log out", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void addSearchEditDeleteEmployeeAndLogout(Config config)
    {
        OrangeHRMHelper orangehrm = new OrangeHRMHelper(config);
        OrangeHRMData employee = new OrangeHRMBuilder().build();

        config.logStep("Login to OrangeHRM as Admin and validate the Dashboard page is shown");
        orangehrm.dashboardPage = orangehrm.login();
        AssertHelper.assertEquals(config, orangehrm.dashboardPage.getPageTitle(), "Dashboard", "Dashboard page title should be 'Dashboard'");

        config.logStep("Open PIM from the left menu, go to the Add Employee tab, fill the employee's name, record the Employee Id the page has already filled in, click Save and validate the Personal Details page opens with 'Arjun Verma' shown as the employee name at the top");
        orangehrm.employeeListPage = orangehrm.dashboardPage.navigateToPim();
        orangehrm.addEmployeePage = orangehrm.employeeListPage.goToAddEmployeeTab();
        orangehrm.addEmployeePage.fillEmployeeName(employee);
        orangehrm.personalDetailsPage = orangehrm.addEmployeePage.save();
        AssertHelper.assertEquals(config, orangehrm.personalDetailsPage.getEmployeeNameHeader(), "Arjun Verma", "Personal Details header should show the employee name");
        String employeeId = orangehrm.personalDetailsPage.getEmployeeId();

        config.logStep("On Personal Details fill Nationality 'Indian', Marital Status 'Single', Date of Birth '1990-15-08' and Gender 'Male', click the Personal Details section's Save button and validate the 'Successfully Updated' toast appears");
        orangehrm.personalDetailsPage.fillPersonalDetails(employee);
        orangehrm.personalDetailsPage = orangehrm.personalDetailsPage.savePersonalDetails();
        String updateToastMessage = orangehrm.personalDetailsPage.getToastMessage();
        AssertHelper.assertEquals(config, updateToastMessage, "Successfully Updated", "Toast should confirm the update");

        config.logStep("Go to the Employee List tab, enter the recorded Employee Id in the Employee Id filter, click Search and validate '(1) Record Found' is shown with the same Id, First (& Middle) Name 'Arjun Kumar' and Last Name 'Verma'");
        orangehrm.employeeListPage = orangehrm.personalDetailsPage.goToEmployeeListTab();
        orangehrm.employeeListPage = orangehrm.employeeListPage.searchByEmployeeId(employeeId);
        AssertHelper.assertEquals(config, orangehrm.employeeListPage.getRecordFoundText(), "(1) Record Found", "Search should find exactly one record");
        AssertHelper.assertEquals(config, orangehrm.employeeListPage.getEmployeeIdInRow(), employeeId, "Row Employee Id should match the recorded Id");
        AssertHelper.assertEquals(config, orangehrm.employeeListPage.getFirstAndMiddleNameInRow(), "Arjun Kumar", "Row should show First & Middle Name");
        AssertHelper.assertEquals(config, orangehrm.employeeListPage.getLastNameInRow(), "Verma", "Row should show Last Name");

        config.logStep("Click the pencil icon on that row and validate Personal Details still shows Nationality 'Indian', Marital Status 'Single', Date of Birth '1990-15-08' and Gender 'Male'");
        orangehrm.personalDetailsPage = orangehrm.employeeListPage.clickEditOnRow();
        AssertHelper.assertEquals(config, orangehrm.personalDetailsPage.getNationality(), "Indian", "Nationality should persist");
        AssertHelper.assertEquals(config, orangehrm.personalDetailsPage.getMaritalStatus(), "Single", "Marital Status should persist");
        AssertHelper.assertEquals(config, orangehrm.personalDetailsPage.getDateOfBirth(), employee.getDateOfBirth(), "Date of Birth should persist");
        AssertHelper.assertEquals(config, orangehrm.personalDetailsPage.getGender(), "Male", "Gender should persist");

        config.logStep("Go back to the Employee List tab, search again by the recorded Employee Id, click the trash icon on that row and validate the 'Are you Sure?' dialog appears");
        orangehrm.employeeListPage = orangehrm.personalDetailsPage.goToEmployeeListTab();
        orangehrm.employeeListPage = orangehrm.employeeListPage.searchByEmployeeId(employeeId);
        orangehrm.deleteConfirmationDialog = orangehrm.employeeListPage.clickDeleteOnRow();
        String confirmationMessage = orangehrm.deleteConfirmationDialog.getConfirmationMessage();
        AssertHelper.assertEquals(config, confirmationMessage, "Are you Sure?", "Delete dialog should ask for confirmation");

        config.logStep("Click 'Yes, Delete', validate the 'Successfully Deleted' toast appears and then 'No Records Found' is shown for that Employee Id");
        orangehrm.employeeListPage = orangehrm.deleteConfirmationDialog.confirmDelete();
        String deleteToastMessage = orangehrm.employeeListPage.getToastMessage();
        AssertHelper.assertEquals(config, deleteToastMessage, "Successfully Deleted", "Toast should confirm the deletion");
        AssertHelper.assertEquals(config, orangehrm.employeeListPage.getRecordFoundText(), "No Records Found", "Deleted employee should no longer be found");

        config.logStep("Open the user menu at the top right, click Logout and validate the Login page is shown again");
        orangehrm.loginPage = orangehrm.employeeListPage.logout();
        AssertHelper.assertElementVisible(config, orangehrm.loginPage.getLoginButton(), "Login page");
    }
}
