package automation.toolshop;

import org.testng.annotations.Test;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Enums.*;
import automation.modules.toolshop.ToolshopBuilder;
import automation.modules.toolshop.ToolshopData;
import automation.modules.toolshop.ToolshopHelper;
import automation.modules.toolshop.ToolshopEnums.PaymentMethod;

public class ToolshopWebTest extends TestBase
{

    @Test(description = "Search for a product, add it to the cart, check out as a logged-in customer with a credit card, and verify the invoice matches everything entered along the way", dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(automatedBy = QA.Mukesh)
    public void searchAddToCartCheckoutAndVerifyInvoice(Config config)
    {
        ToolshopHelper toolshop = new ToolshopHelper(config);
        ToolshopData order = new ToolshopBuilder().build();
        String email = config.getRunTimeProperty("toolshop.username");
        String password = config.getRunTimeProperty("toolshop.password");

        config.logStep("Search for 'Combination Pliers' on the home page and verify exactly one product is found with that name");
        toolshop.productListPage = toolshop.searchProduct(order.getProductName());
        AssertHelper.assertEquals(config, toolshop.productListPage.getResultsCount(), 1, "Exactly one product should be found");
        AssertHelper.assertEquals(config, toolshop.productListPage.getFirstProductName(), "Combination Pliers", "First product name should match the search");

        config.logStep("Open the product and verify the details page shows the same product name; note the price shown");
        toolshop.productDetailsPage = toolshop.productListPage.openProduct(order.getProductName());
        order.setPrice(toolshop.productDetailsPage.getPrice());
        AssertHelper.assertEquals(config, toolshop.productDetailsPage.getProductName(), "Combination Pliers", "Details page should show the same product name");

        config.logStep("Increase the quantity to 2 and add the product to the cart; verify the toast appears and the cart icon shows 2");
        toolshop.productDetailsPage = toolshop.addProductToCart(order);
        AssertHelper.assertEquals(config, toolshop.productDetailsPage.getToastMessage(), "Product added to shopping cart.", "Toast message should confirm the product was added");
        AssertHelper.assertEquals(config, toolshop.productDetailsPage.getCartCount(), "2", "Cart icon should show 2 items");

        config.logStep("Open the cart and verify the product name, quantity and price match the search and the noted price; note the cart Total");
        toolshop.cartPage = toolshop.productDetailsPage.openCart();
        order.setCartTotal(toolshop.cartPage.getTotal());
        AssertHelper.assertEquals(config, toolshop.cartPage.getProductName().trim().replaceAll("(?U)\\s+", " "), "Combination Pliers".trim().replaceAll("(?U)\\s+", " "), "Cart should show the same product name");
        AssertHelper.assertEquals(config, toolshop.cartPage.getQuantity(), "2", "Cart should show quantity 2");
        AssertHelper.assertEquals(config, toolshop.cartPage.getPrice(), order.getPrice(), "Cart price should match the price noted on the details page");

        config.logStep("Proceed to checkout and sign in with the customer's credentials; verify the already-logged-in message is shown");
        toolshop.checkoutSignInPage = toolshop.cartPage.proceedToCheckout();
        toolshop.checkoutSignInPage = toolshop.checkoutSignInPage.signIn(email, password);
        AssertHelper.assertEquals(config, toolshop.checkoutSignInPage.getLoggedInMessage(), "Hello Jack Howe, you are already logged in. You can proceed to checkout.", "Logged-in message should greet Jack Howe");

        config.logStep("Proceed to checkout, fill the billing address (Country, Postal code, House number), wait for it to be looked up automatically, and note the Street and City before proceeding to checkout");
        toolshop.checkoutBillingPage = toolshop.checkoutSignInPage.proceedToCheckout();
        toolshop.checkoutPaymentPage = toolshop.submitBillingAddress(order);

        config.logStep("Choose Credit Card payment, fill the card details, and check the payment; verify the 'Payment was successful' message appears");
        toolshop.checkoutPaymentPage = toolshop.checkoutPaymentPage.choosePaymentMethod(PaymentMethod.CreditCard);
        toolshop.checkoutPaymentPage = toolshop.checkoutPaymentPage.fillCardDetails(order);
        toolshop.checkoutPaymentPage = toolshop.checkoutPaymentPage.checkPayment();
        AssertHelper.assertEquals(config, toolshop.checkoutPaymentPage.getPaymentMessage(), "Payment was successful", "Payment message should confirm success");

        config.logStep("Click 'Confirm' and verify the confirmation shows the thanks message with an invoice number; note the invoice number");
        toolshop.checkoutConfirmationPage = toolshop.checkoutPaymentPage.confirmOrder();
        order.setInvoiceNumber(toolshop.checkoutConfirmationPage.getInvoiceNumber());
        AssertHelper.assertContains(config, toolshop.checkoutConfirmationPage.getConfirmationMessage(), "Thanks for your order! Your invoice number is", "Confirmation message should contain the thanks and invoice phrase");

        config.logStep("Open the user menu and go to My invoices; verify the first row shows the noted invoice number and its Total matches the noted cart Total");
        toolshop.myInvoicesPage = toolshop.checkoutConfirmationPage.openMyInvoices();
        AssertHelper.assertEquals(config, toolshop.myInvoicesPage.getFirstRowInvoiceNumber(), order.getInvoiceNumber(), "First row invoice number should match the one noted earlier");
        AssertHelper.assertEquals(config, toolshop.myInvoicesPage.getFirstRowTotal(), order.getCartTotal(), "First row total should match the cart total noted earlier");

        config.logStep("Click Details on that row and verify all the invoice details match what was entered and noted earlier");
        toolshop.invoiceDetailsPage = toolshop.myInvoicesPage.openDetails();
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getInvoiceNumber(), order.getInvoiceNumber(), "Invoice number should match the one noted earlier");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getTotal(), order.getCartTotal(), "Total should match the cart total noted earlier");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getStreet(), order.getStreet(), "Street should match the one filled automatically");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getCity(), order.getCity(), "City should match the one filled automatically");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getPostalCode(), "60311", "Postal code should match the one entered");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getPaymentMethod(), "Credit Card", "Payment method should be Credit Card");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getCardHolderName(), "Jack Howe", "Card holder name should match the one entered");
        AssertHelper.assertEquals(config, toolshop.invoiceDetailsPage.getProductRowQuantity("Combination Pliers"), "2", "Products table should list the product with quantity 2");

        config.logStep("Sign out from the user menu and verify the 'Sign in' link is back in the header");
        toolshop.homePage = toolshop.invoiceDetailsPage.signOut();
        AssertHelper.assertElementVisible(config, toolshop.homePage.signInLink, "Sign in link");
    }
}
