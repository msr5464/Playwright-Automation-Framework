package automation.core;

public class Enums
{

    public enum UserType
    {
        Admin, FinanceTransfer, FinanceSubmit, Employee, Readonly, Any
    }

    public enum Feature
    {
        CARD, BUDGET, CLAIM, DBS_SG, DBS_HK, CC_SG, CALASTONE_SG
    }

    public enum Country
    {
        SG, HK, US, AU, ID, VN;

        public String toLowerCase()
        {
            return name().toLowerCase();
        }
    }

    public enum AppLanguage
    {
        EN, ID, VI, TH
    }

    public enum ProjectName
    {
        GitHub, SauceDemo
    }

    public enum DatabaseName
    {
        Thanos, QA_Dashboard, Staging
    }

    public enum QueryType
    {
        select, update, delete, create, set
    }

    public enum VideoMode
    {
        ON, ON_FAILURE, OFF;

        public static VideoMode fromString(String value)
        {
            if (value == null) return OFF;
            return switch (value.toLowerCase())
            {
                case "on" -> ON;
                case "on_failure", "on-failure" -> ON_FAILURE;
                default -> OFF;
            };
        }
    }

    public enum QA
    {
        Mukesh, Unassigned
    }

    public enum Currency
    {
        SGD, HKD, USD, AUD, EUR, GBP, INR, IDR, VND, THB, PHP, MYR, CNY, JPY, KRW, NZD, CAD, CHF
    }

    public enum CardColor
    {
        SpringGreen, BerryBlue, BlazeOrange, RubyRed, IrisPurple, MidnightBlue
    }

    public enum TransferCategory
    {
        Entertainment, Business, Travel, Medical, Education, Other
    }
}
