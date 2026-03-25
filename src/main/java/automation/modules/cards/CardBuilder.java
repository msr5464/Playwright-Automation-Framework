package automation.modules.cards;

import automation.core.Enums.CardColor;
import automation.core.DataGenerator;

import java.util.Random;

/**
 * Fluent builder for CardData with domain-aware defaults.
 */
public class CardBuilder
{

    private String cardName;
    private String cardPurpose = "General expenses";
    private String sourceOfFunds = "Company";
    private String cardColor;
    private String spendingLimit = "1000";
    private String cardType = "Virtual";

    public CardBuilder withCardName(String cardName)
    {
        this.cardName = cardName;
        return this;
    }

    public CardBuilder withCardPurpose(String purpose)
    {
        this.cardPurpose = purpose;
        return this;
    }

    public CardBuilder withSourceOfFunds(String source)
    {
        this.sourceOfFunds = source;
        return this;
    }

    public CardBuilder withCardColor(String color)
    {
        this.cardColor = color;
        return this;
    }

    public CardBuilder withRandomColor()
    {
        CardColor[] colors = CardColor.values();
        this.cardColor = colors[new Random().nextInt(colors.length)].name();
        return this;
    }

    public CardBuilder withSpendingLimit(String limit)
    {
        this.spendingLimit = limit;
        return this;
    }

    public CardBuilder withCardType(String type)
    {
        this.cardType = type;
        return this;
    }

    public CardBuilder withDefaults()
    {
        if (this.cardName == null) this.cardName = "Card_" + DataGenerator.randomAlphaString(5);
        if (this.cardColor == null) withRandomColor();
        return this;
    }

    public CardData build()
    {
        withDefaults();
        CardData card = new CardData();
        card.setCardName(cardName);
        card.setCardPurpose(cardPurpose);
        card.setSourceOfFunds(sourceOfFunds);
        card.setCardColor(cardColor);
        card.setSpendingLimit(spendingLimit);
        card.setCardType(cardType);
        return card;
    }
}
