package automation.modules.paymentgateway;

public class PaymentGatewayEnums
{
    public enum PaymentMethod
    {
        CreditCard("#/credit-card", "Card Payment"),
        /** Shown 2 times on the payment popup, so its locator is not unique there. */
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

    public enum Promo
    {
        FlashSaleCreditCard("690", "Promo Flash Sale (Credit-Card)"),
        CreditCardPer3CardNumber("628", "Promo Credit-Card (Per 3 Card Number)"),
        NoPromo("no-promo", "Proceed without promo");

        private final String key;
        private final String label;

        Promo(String key, String label)
        {
            this.key = key;
            this.label = label;
        }

        public String getKey()   { return key; }
        public String getLabel() { return label; }
    }
}
