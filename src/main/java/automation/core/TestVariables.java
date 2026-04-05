package automation.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import automation.core.Enums.Country;
import automation.core.Enums.QA;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestVariables
{
    String testrailData() default "";
    QA automatedBy() default QA.Unassigned;
    QA maintainedBy() default QA.Unassigned;
    Country country() default Country.SG;
}
