package automation.aiEval.normalizer;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.RunContext;
import automation.aiEval.runner.RawServiceResponse;

public interface ResponseNormalizer
{
    AIExecutionResult normalize(RawServiceResponse raw, AITestCase testCase, RunContext runContext);
}
