package automation.modules.orangehrm;

/**
 * Fluent builder for OrangeHRMData with sensible defaults.
 * firstName, middleName, lastName and dateOfBirth default to the exact values the test
 * case was given; employeeId is response-only and has no setter here - it is read back
 * from the Add Employee page after save.
 */
public class OrangeHRMBuilder
{
    private String firstName;
    private String middleName;
    private String lastName;
    private String nationality;
    private String maritalStatus;
    private String dateOfBirth;
    private String gender;

    public OrangeHRMBuilder withFirstName(String firstName)
    {
        this.firstName = firstName;
        return this;
    }

    public OrangeHRMBuilder withMiddleName(String middleName)
    {
        this.middleName = middleName;
        return this;
    }

    public OrangeHRMBuilder withLastName(String lastName)
    {
        this.lastName = lastName;
        return this;
    }

    public OrangeHRMBuilder withNationality(String nationality)
    {
        this.nationality = nationality;
        return this;
    }

    public OrangeHRMBuilder withMaritalStatus(String maritalStatus)
    {
        this.maritalStatus = maritalStatus;
        return this;
    }

    public OrangeHRMBuilder withDateOfBirth(String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public OrangeHRMBuilder withGender(String gender)
    {
        this.gender = gender;
        return this;
    }

    public OrangeHRMBuilder withDefaults()
    {
        if (firstName == null) firstName = "Arjun";
        if (middleName == null) middleName = "Kumar";
        if (lastName == null) lastName = "Verma";
        if (nationality == null) nationality = "Indian";
        if (maritalStatus == null) maritalStatus = "Single";
        if (dateOfBirth == null) dateOfBirth = "1990-15-08";
        if (gender == null) gender = "Male";
        return this;
    }

    public OrangeHRMData build()
    {
        withDefaults();
        OrangeHRMData employee = new OrangeHRMData();
        employee.setFirstName(firstName);
        employee.setMiddleName(middleName);
        employee.setLastName(lastName);
        employee.setNationality(nationality);
        employee.setMaritalStatus(maritalStatus);
        employee.setDateOfBirth(dateOfBirth);
        employee.setGender(gender);
        return employee;
    }
}
