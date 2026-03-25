package automation.modules.cards.web;

import com.microsoft.playwright.Locator;

import automation.core.Config;
import automation.core.BasePage;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.modules.cards.CardData;

public class CardPage extends BasePage
{

    private final Locator cardNameField;
    private final Locator cardPurposeField;
    private final Locator spendingLimitField;
    private final Locator sourceOfFundsDropdown;
    private final Locator submitButton;
    private final Locator confirmButton;
    private final Locator successMessage;
    private final Locator cardNameDisplay;
    private final Locator spendingLimitDisplay;

    public CardPage(Config config)
    {
        super(config);
        cardNameField        = page.locator("[data-cy='card-name-input'], input[name='cardName']");
        cardPurposeField     = page.locator("[data-cy='card-purpose-input'], input[name='cardPurpose']");
        spendingLimitField   = page.locator("[data-cy='spending-limit-input'], input[name='spendingLimit']");
        sourceOfFundsDropdown = page.locator("[data-cy='source-of-funds'], select[name='sourceOfFunds']");
        submitButton         = page.locator("[data-cy='submit-button'], button[type='submit']");
        confirmButton        = page.locator("[data-cy='confirm-button'], button:has-text('Confirm')");
        successMessage       = page.locator("[data-cy='success-message'], .success-message");
        cardNameDisplay      = page.locator("[data-cy='card-name-display']");
        spendingLimitDisplay = page.locator("[data-cy='spending-limit-display']");
        waitUntilLoaded();
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, cardNameField, "Card form");
    }

    /**
     * Fill the create-card form using a CardData POJO.
     */
    public void fillCardForm(CardData card)
    {
        Log.step(config, "Filling card form: " + card.getCardName());
        fillText(cardNameField, card.getCardName(), "Card name");
        fillText(cardPurposeField, card.getCardPurpose(), "Card purpose");
        fillText(spendingLimitField, card.getSpendingLimit(), "Spending limit");

        if (card.getSourceOfFunds() != null)
        {
            selectOption(sourceOfFundsDropdown, card.getSourceOfFunds(), "Source of funds");
        }

        if (card.getCardColor() != null)
        {
            Locator colorOption = page.locator("[data-cy='color-" + card.getCardColor() + "']");
            if (isElementDisplayed(colorOption))
            {
                click(colorOption, "Card color: " + card.getCardColor());
            }
        }
    }

    public void submitForm()
    {
        click(submitButton, "Submit button");
        waitForLoadingComplete();
    }

    public void confirmCreation()
    {
        clickUntilNextElementIsLoaded(confirmButton, successMessage, "Confirm button", "Success message");
    }

    /**
     * Full create-card flow: fill form -> submit -> confirm.
     */
    public void createCard(CardData card)
    {
        fillCardForm(card);
        submitForm();
        confirmCreation();
        Log.pass(config, "Card created via UI: " + card.getCardName());
    }

    public String getDisplayedCardName()
    {
        return getText(cardNameDisplay, "Card name display");
    }

    public String getDisplayedSpendingLimit()
    {
        return getText(spendingLimitDisplay, "Spending limit display");
    }

    public boolean isSuccessMessageVisible()
    {
        return isElementDisplayed(successMessage);
    }
}
