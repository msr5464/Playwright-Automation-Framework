package automation.cards;

import org.testng.annotations.Test;

import automation.modules.cards.CardBuilder;
import automation.modules.cards.CardData;
import automation.modules.cards.CardHelper;
import automation.modules.cards.api.CardApi;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.core.User;
import automation.core.AssertHelper;

public class CardApiTest extends TestBase
{

    @Test(description="verify if user is able to create new card", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void createAndVerifyCard(Config config)
    {

        User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

        CardHelper cards = new CardHelper(config);
        cards.loginAndSetAuth(user);

        // Create
        CardData card = new CardBuilder().withCardName("Marketing Card").withSpendingLimit("5000").build();
        CardData created = cards.createCard(card);

        // Read
        CardData fetched = cards.getCard(created.getId());
        AssertHelper.assertEquals(config, fetched.getCardName(), "Marketing Card", "Card name matches");

        // Update
        CardData updateRequest = new CardBuilder().withSpendingLimit("10000").build();
        cards.updateCard(created.getId(), updateRequest);

        // Delete
        cards.deleteCard(created.getId());

    }

    @Test(description="verify user is NOT able to create new card if some fields are missing",dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void createCard_withMissingFields(Config config)
    {

        CardHelper cards = new CardHelper(config);

        CardData incomplete = new CardBuilder().withCardName("Incomplete").build();
        var response = cards.executeRaw(CardApi.CreateCard, incomplete);
        AssertHelper.assertEquals(config, response.getStatusCode(), 400, "Should reject incomplete card");

    }
}
