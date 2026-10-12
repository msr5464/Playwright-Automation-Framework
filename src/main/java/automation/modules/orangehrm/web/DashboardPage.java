package automation.modules.orangehrm.web;

import com.microsoft.playwright.Locator;

import automation.core.BasePage;
import automation.core.Config;
import automation.core.Log;

public class DashboardPage extends BasePage
{
    private final Locator dashboardHeader;
    private final Locator pimMenuItem;

    public DashboardPage(Config config)
    {
        super(config);
        dashboardHeader = page.locator("h6:has-text('Dashboard')");
        pimMenuItem     = page.locator("a[href='/web/index.php/pim/viewPimModule']");
        assertPageLoaded(dashboardHeader);
    }

    public String getPageTitle()
    {
        return getText(dashboardHeader, "Dashboard page title");
    }

    public EmployeeListPage navigateToPim()
    {
        Log.comment(config, "Navigating to PIM module");
        click(pimMenuItem, "PIM menu item");
        return new EmployeeListPage(config);
    }
}
