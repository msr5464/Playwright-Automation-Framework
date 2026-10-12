package automation.modules.toolshop;

import automation.core.BrowserHelper;
import automation.core.Config;
import automation.core.Log;
import automation.core.WaitHelper;
import automation.core.api.ApiHelper;
import automation.modules.toolshop.ToolshopEnums.PaymentMethod;
import automation.modules.toolshop.web.CartPage;
import automation.modules.toolshop.web.CheckoutBillingAddressPage;
import automation.modules.toolshop.web.CheckoutConfirmationPage;
import automation.modules.toolshop.web.CheckoutPaymentPage;
import automation.modules.toolshop.web.CheckoutSignInPage;
import automation.modules.toolshop.web.HomePage;
import automation.modules.toolshop.web.InvoiceDetailsPage;
import automation.modules.toolshop.web.MyInvoicesPage;
import automation.modules.toolshop.web.ProductDetailsPage;
import automation.modules.toolshop.web.ProductListPage;

import java.math.BigDecimal;

/**
 * Business operations for the Toolshop web flow. Holds one public field per page of
 * the module; every action that leaves a page returns the next page object, and the
 * test stores it on that page's field here, so each step continues from the page the
 * previous step returned.
 *
 * Usage:
 *   ToolshopHelper toolshop = new ToolshopHelper(config);
 *   toolshop.productListPage = toolshop.searchProduct("Combination Pliers");
 *   toolshop.productDetailsPage = toolshop.productListPage.openProduct("Combination Pliers");
 *   toolshop.productDetailsPage = toolshop.addProductToCart(order);
 */
public class ToolshopHelper extends ApiHelper
{
    public HomePage homePage;
    public ProductListPage productListPage;
    public ProductDetailsPage productDetailsPage;
    public CartPage cartPage;
    public CheckoutSignInPage checkoutSignInPage;
    public CheckoutBillingAddressPage checkoutBillingPage;
    public CheckoutPaymentPage checkoutPaymentPage;
    public CheckoutConfirmationPage checkoutConfirmationPage;
    public MyInvoicesPage myInvoicesPage;
    public InvoiceDetailsPage invoiceDetailsPage;

    public ToolshopHelper(Config config)
    {
        super(config, config.getRunTimeProperty("toolshop.url"));
    }

    /**
     * Open Toolshop and search for a product. The entry operation: the only place a
     * page object is constructed, right after navigating. Returns the Product list
     * page it lands on.
     */
    public ProductListPage searchProduct(String productName)
    {
        String url = config.getRunTimeProperty("toolshop.url");
        Log.comment(config, "Navigating to Toolshop: " + url);
        BrowserHelper.navigateTo(config, url);
        homePage = new HomePage(config);
        productListPage = homePage.searchProduct(productName);
        return productListPage;
    }

    /**
     * Increase the quantity on the Product details page to match the order's
     * quantity and add it to the cart, continuing from productDetailsPage. Returns
     * the Product details page it lands on.
     */
    public ProductDetailsPage addProductToCart(ToolshopData order)
    {
        int targetQuantity = Integer.parseInt(order.getQuantity());
        for (int i = 1; i < targetQuantity; i++)
        {
            productDetailsPage = productDetailsPage.increaseQuantity();
        }
        productDetailsPage = productDetailsPage.addToCart();
        return productDetailsPage;
    }

    /**
     * Fill the billing address on the Billing address page, wait for the autofilled
     * fields to settle, record the Street and City onto the order, and proceed to
     * the Payment step. Continues from checkoutBillingPage. Returns the Payment page
     * it lands on.
     */
    public CheckoutPaymentPage submitBillingAddress(ToolshopData order)
    {
        checkoutBillingPage = checkoutBillingPage.fillBillingAddress(order);
        order.setStreet(checkoutBillingPage.getStreet());
        order.setCity(checkoutBillingPage.getCity());
        checkoutPaymentPage = checkoutBillingPage.proceedToCheckout();
        return checkoutPaymentPage;
    }

    /**
     * Choose Credit Card payment, fill the card details, check the payment, and
     * confirm the order, for a test that does not need to check the intermediate
     * 'Payment was successful' message. Continues from checkoutPaymentPage. Returns
     * the Confirmation page it lands on.
     */
    public CheckoutConfirmationPage payByCreditCard(ToolshopData order)
    {
        checkoutPaymentPage = checkoutPaymentPage.choosePaymentMethod(PaymentMethod.CreditCard);
        checkoutPaymentPage = checkoutPaymentPage.fillCardDetails(order);
        checkoutPaymentPage = checkoutPaymentPage.checkPayment();
        checkoutConfirmationPage = checkoutPaymentPage.confirmOrder();
        return checkoutConfirmationPage;
    }

    /**
     * The module's one converter: turns a displayed amount ('$14.15') into plain
     * number text (no currency, no grouping separators, no trailing decimal zeros)
     * so two amounts shown in different formats can be compared as strings.
     */
    public static String toPlainAmount(String shown)
    {
        String digitsOnly = shown.replaceAll("[^0-9.]", "");
        if (digitsOnly.isEmpty())
        {
            return digitsOnly;
        }
        BigDecimal amount = new BigDecimal(digitsOnly).stripTrailingZeros();
        return amount.toPlainString();
    }
}
