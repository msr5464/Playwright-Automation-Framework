package automation.core.ai;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the dataset an eval test runs on: every {@code .json} file under
 * {@code src/test/resources/{module}/evalCases/{name}/}, one test invocation per case.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EvalDataset
{
    String module();

    String name();
}
