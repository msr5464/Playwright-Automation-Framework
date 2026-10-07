package automation.modules.paymentgateway;

public class PaymentGatewayEnums
{
    /**
     * Payment method options offered on the PaymentMethodPage. Only CreditCard is
     * automated; every other value throws UnsupportedOperationException when selected.
     * GoPayQris's locator is shown 2 times on that page, so it is not unique there.
     */
    public enum PaymentMethod
    {
        CreditCard("#/credit-card", "Card Payment"),
        GoPayQris("#/gopay-qris", "GoPay QRIS"),
        ShopeePayQris("#/shopeepay-qris", "ShopeePay QRIS"),
        OtherQris("#/other-qris", "QRIS"),
        AlfaGroup("#/alfamart", "Alfa Group"),
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
}
