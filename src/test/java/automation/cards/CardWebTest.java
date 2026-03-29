package automation.cards;

import org.testng.annotations.Test;

import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.cards.CardBuilder;
import automation.modules.cards.CardData;
import automation.modules.cards.CardHelper;
import automation.core.User;
import automation.modules.cards.web.CardListPage;
import automation.modules.cards.web.CardPage;
import automation.modules.access.web.DashboardPage;
import automation.core.AssertHelper;

public class CardWebTest extends TestBase
{

    @Test(description="verify if user is able to create new card via UI", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C0001:WEB", automatedBy = QA.Mukesh, country = Country.SG)
    public void createCardViaUI(Config config)
    {

        User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

        CardData card = new CardBuilder()
            .withCardName("Marketing Card")
            .withCardPurpose("Team expenses")
            .withSourceOfFunds("Company")
            .withSpendingLimit("5000")
            .withRandomColor()
            .build();
        ctx().addCard("marketing", card);

        CardHelper cards = new CardHelper(config);
        DashboardPage dashboard = cards.doLogin(user);
        CardPage cardPage = cards.createCardViaUI(dashboard, card);

        AssertHelper.assertTrue(config, cardPage.isSuccessMessageVisible(), "Success message should appear");
        AssertHelper.assertEquals(config, cardPage.getDisplayedCardName(), "Marketing Card", "Card name on detail page");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C0002:WEB", automatedBy = QA.Mukesh, country = Country.SG)
    public void verifyCardListPage(Config config)
    {

        User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

        CardHelper cards = new CardHelper(config);
        DashboardPage dashboard = cards.doLogin(user);
        CardListPage cardList = dashboard.navigateToCards();

        AssertHelper.assertTrue(config, cardList.getCardCount() >= 0, "Card list should load without errors");
        cardList.searchCard("Marketing");
    }

    @Test(description="verify if admin user is able to create new card and then same card is verifed by employee user",dataProvider = "getTwoConfigs", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C0003:WEB", automatedBy = QA.Mukesh, country = Country.SG)
    public void adminCreatesCard_employeeVerifies(Config adminConfig, Config employeeConfig)
    {

        User admin = allocateUser(adminConfig, UserType.Admin, Feature.CARD, Country.SG);
        User employee = allocateUser(employeeConfig, UserType.Employee, Feature.CARD, Country.SG);

        CardData card = new CardBuilder().withCardName("Shared Card").withSpendingLimit("3000").build();

        CardHelper adminCards = new CardHelper(adminConfig);
        DashboardPage adminDashboard = adminCards.doLogin(admin);
        adminCards.createCardViaUI(adminDashboard, card);

        CardHelper empCards = new CardHelper(employeeConfig);
        DashboardPage empDashboard = empCards.doLogin(employee);
        CardListPage empCardList = empDashboard.navigateToCards();

        empCardList.searchCard("Shared Card");
        AssertHelper.assertTrue(employeeConfig, empCardList.isCardVisible("Shared Card"),
            "Employee should see the card created by admin");
    }
}
