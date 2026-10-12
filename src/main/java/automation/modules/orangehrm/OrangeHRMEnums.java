package automation.modules.orangehrm;

public class OrangeHRMEnums
{
    /**
     * Gender selected on the Personal Details page.
     * No alternatives were recorded during validation — only Male was exercised.
     */
    public enum Gender
    {
        Male("Male", "Male");

        private final String key;
        private final String label;

        Gender(String key, String label)
        {
            this.key = key;
            this.label = label;
        }

        public String getKey()   { return key; }
        public String getLabel() { return label; }
    }
}
