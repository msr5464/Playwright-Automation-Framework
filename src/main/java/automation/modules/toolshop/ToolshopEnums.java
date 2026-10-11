package automation.modules.toolshop;

/**
 * Option enums for the Toolshop module — one nested enum per choice the UI offers.
 */
public class ToolshopEnums
{
    /**
     * Payment method chosen on the Payment step. Only CreditCard was exercised and
     * recorded; no alternatives were observed on the live page.
     */
    public enum PaymentMethod
    {
        CreditCard("credit-card", "Credit Card");

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
