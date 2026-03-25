package automation.modules.access.web;

import com.microsoft.playwright.Locator;

import automation.core.Config;
import automation.core.BasePage;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.cards.web.CardListPage;

public class DashboardPage extends BasePage
{

    private final Locator pageIndicator;
    private final Locator sidebarMenu;
    private final Locator cardsMenuItem;
    private final Locator transfersMenuItem;
    private final Locator budgetsMenuItem;
    private final Locator claimsMenuItem;
    private final Locator businessSwitcher;

    public DashboardPage(Config config)
    {
        super(config);
        pageIndicator    = page.locator("[data-cy='dashboard'], .dashboard-container");
        sidebarMenu      = page.locator("[data-cy='dashboard-sidebar-menu-item']");
        cardsMenuItem    = page.locator("[data-cy='sidebar-cards'], a:has-text('Cards')");
        transfersMenuItem = page.locator("[data-cy='sidebar-transfers'], a:has-text('Transfers')");
        budgetsMenuItem  = page.locator("[data-cy='sidebar-budgets'], a:has-text('Budgets')");
        claimsMenuItem   = page.locator("[data-cy='sidebar-claims'], a:has-text('Claims')");
        businessSwitcher = page.locator("[data-cy='dashboard-businesses-switcher']");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, pageIndicator, "Dashboard");
    }

    public CardListPage navigateToCards()
    {
        navigateToFeature(cardsMenuItem, "Cards");
        return new CardListPage(config);
    }

    public void navigateToTransfers()
    {
        navigateToFeature(transfersMenuItem, "Transfers");
    }

    public void navigateToBudgets()
    {
        navigateToFeature(budgetsMenuItem, "Budgets");
    }

    public void navigateToClaims()
    {
        navigateToFeature(claimsMenuItem, "Claims");
    }

    public void switchBusiness(String businessName)
    {
        Log.step(config, "Switching to business: " + businessName);
        click(businessSwitcher, "Business switcher");
        Locator businessOption = page.locator("text=" + businessName);
        click(businessOption, "Business: " + businessName);
        waitForLoadingComplete();
    }

}
