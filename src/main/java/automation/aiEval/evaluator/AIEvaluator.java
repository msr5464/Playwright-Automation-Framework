package automation.aiEval.evaluator;

import automation.aiEval.model.AIExecutionResult;
import automation.aiEval.model.AITestCase;
import automation.aiEval.model.EvaluationResult;

public interface AIEvaluator
{
    EvaluationResult evaluate(AITestCase testCase, AIExecutionResult result);

    String getName();
}
