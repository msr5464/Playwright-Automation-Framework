package automation.modules.paymentgateway;

public class PaymentGatewayEnums
{
    /**
     * Payment method picked on the payment popup. Only CreditCard has been
     * exercised by an automated flow; the other keys are recorded here from
     * the page's own option set for completeness but are not automated yet.
     */
    public enum PaymentMethod
    {
        CreditCard("#/credit-card", "Card Payment"),
        // Shown 2 times on the payment popup, so its locator is not unique there.
        GoPayQris("#/gopay-qris", "GoPay QRIS"),
        ShopeePayQris("#/shopeepay-qris", "ShopeePay QRIS"),
        OtherQris("#/other-qris", "QRIS"),
        Alfamart("#/alfamart", "Alfa Group"),
        Indomaret("#/indomaret", "Indomaret");

        private final String key;
        private final String label;

        PaymentMethod(String key, String label)
        {
            this.key = key;
            this.label = label;
        }

        public String getKey()   { return key; }
        public String getLabel() { return label; }
    }

    /**
     * Promo code applied on the credit card form. No alternative promo codes
     * were recorded during validation — only FlashSaleCreditCard is listed.
     */
    public enum PromoCode
    {
        FlashSaleCreditCard("flash-sale-credit-card", "Promo Flash Sale (Credit-Card)");

        private final String key;
        private final String label;

        PromoCode(String key, String label)
        {
            this.key = key;
            this.label = label;
        }

        public String getKey()   { return key; }
        public String getLabel() { return label; }
    }
}
