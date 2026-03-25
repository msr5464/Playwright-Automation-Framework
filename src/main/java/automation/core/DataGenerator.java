package automation.core;

import net.datafaker.Faker;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

public class DataGenerator
{

    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    public static String getCurrentDateTime(String format)
    {
        return DateTime.now().toString(DateTimeFormat.forPattern(format));
    }

    public static String getCurrentDateTime(String format, String timezone)
    {
        return DateTime.now(DateTimeZone.forID(timezone)).toString(DateTimeFormat.forPattern(format));
    }

    public static String randomString(int length)
    {
        return faker.lorem().characters(length, true, true);
    }

    public static String randomAlphaString(int length)
    {
        return faker.lorem().characters(length, false, false);
    }

    public static String randomAlphaNumericString(int length)
    {
        return faker.lorem().characters(length, true, false);
    }

    public static int randomNumber(int min, int max)
    {
        return random.nextInt(max - min + 1) + min;
    }

    public static String randomUUID()
    {
        return UUID.randomUUID().toString();
    }

    public static String randomEmail()
    {
        return faker.internet().emailAddress();
    }

    public static String randomPhoneNumber()
    {
        return faker.phoneNumber().phoneNumber();
    }

    public static String randomFirstName()
    {
        return faker.name().firstName();
    }

    public static String randomLastName()
    {
        return faker.name().lastName();
    }

    public static String randomFullName()
    {
        return faker.name().fullName();
    }

    public static String randomCompanyName()
    {
        return faker.company().name();
    }

    public static String randomAddress()
    {
        return faker.address().fullAddress();
    }

    public static String formatAmount(double amount)
    {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }

    public static String formatAmountWithCurrency(double amount, String currency)
    {
        return currency + " " + formatAmount(amount);
    }

    public static String getDateWithOffset(int daysOffset, String format)
    {
        return DateTime.now().plusDays(daysOffset).toString(DateTimeFormat.forPattern(format));
    }

    public static String getDateWithOffset(int daysOffset, String format, String timezone)
    {
        return DateTime.now(DateTimeZone.forID(timezone))
            .plusDays(daysOffset)
            .toString(DateTimeFormat.forPattern(format));
    }

    public static Faker getFaker()
    {
        return faker;
    }
}
