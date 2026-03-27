package automation.aiEval.runner;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.RunContext;

public interface AIRunner
{
    AIExecutionResult execute(AITestCase testCase, RunContext runContext);
}
