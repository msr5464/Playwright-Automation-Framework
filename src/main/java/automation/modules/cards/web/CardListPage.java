package automation.modules.cards.web;

import com.microsoft.playwright.Locator;

import automation.core.Config;
import automation.core.BasePage;
import automation.core.Log;
import automation.core.WaitHelper;

public class CardListPage extends BasePage
{

    private final Locator createCardButton;
    private final Locator cardList;
    private final Locator searchField;

    public CardListPage(Config config)
    {
        super(config);
        createCardButton = page.locator("[data-cy='create-card-button'], button:has-text('Create card')");
        cardList         = page.locator("[data-cy='card-list'], .card-list-container");
        searchField      = page.locator("[data-cy='card-search'], input[placeholder*='Search']");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, cardList, "Card list");
    }

    public CardPage clickCreateCard()
    {
        click(createCardButton, "Create Card button");
        return new CardPage(config);
    }

    public void searchCard(String cardName)
    {
        fillText(searchField, cardName, "Card search");
        waitForLoadingComplete();
    }

    public boolean isCardVisible(String cardName)
    {
        Locator card = page.locator("[data-cy='card-item']:has-text('" + cardName + "')");
        return isElementDisplayed(card);
    }

    public CardPage openCard(String cardName)
    {
        Log.step(config, "Opening card: " + cardName);
        Locator card = page.locator("[data-cy='card-item']:has-text('" + cardName + "')");
        click(card, "Card: " + cardName);
        return new CardPage(config);
    }

    public int getCardCount()
    {
        return page.locator("[data-cy='card-item']").count();
    }
}
