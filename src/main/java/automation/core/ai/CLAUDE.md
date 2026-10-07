# AI evaluation layer — `automation.core.ai`

Evaluates AI applications (RAG, LLM pipelines, agents) the way the web, API and mobile layers test ordinary apps. The phased learn-and-build plan is [docs/ai-eval/PLAN.md](../../../../../../docs/ai-eval/PLAN.md).

This guide lives here, not in the root `CLAUDE.md`, because QA-Agent-Network puts the root file into its agents' prompts. Keep AI-layer conventions in this file.

## Writing an eval test

```java
public class TalkToTestsEvalTest extends EvalTestBase
{
    @Test(dataProvider = "evalCases", groups = {GROUP_REGRESSION, GROUP_API})
    @EvalDataset(module = "aiteststudio", name = "talk-to-tests")
    @TestVariables(automatedBy = QA.Mukesh)
    public void answersDatasetQuestions(Config config, EvalCase evalCase)
    {
        AiTestStudioHelper studio = new AiTestStudioHelper(config);
        config.logStep("Log in to AI-Test-Studio");
        studio.login();
        config.logStep("Ask Talk to Tests: " + evalCase.getInput());
        EvalResponse response = studio.askTalkToTests(evalCase);
        config.logStep("Score the answer");
        EvalAssert.meets(config, evalCase, response, studio.talkToTestsEvaluators());
    }
}
```

- **Extend `EvalTestBase`, not `TestBase`.**
  - The `evalCases` data provider runs the method once per case, from every `.json` file under `src/test/resources/{module}/evalCases/{name}/`.
  - Each file holds one `EvalCase` or an array of them.
- **Each case gets its own `Config`, with retry off.** A failed case must stay failed; retrying until it passes would report pass@3 as the pass rate.
- **The module's helper is the adapter.** It calls the app and maps the reply into `EvalResponse`, and it also builds the evaluator list, so the test stays declarative. See `automation.modules.aiteststudio.AiTestStudioHelper`.
- **`EvalAssert.meets` scores the case.**
  - It logs each evaluator's result and fails the test softly when the case fails, like `AssertHelper`.
  - It also records the case for the run report.

## How one case flows

1. **`EvalCase`**: one dataset row (`input`, `mustContain`, `mustNotContain`, `riskLevel`, `tags`, `metadata`), loaded by `EvalCaseLoader`.
2. **The adapter** calls the app and returns an **`EvalResponse`**: `output`, `latencyMs`, `costUsd`, `retrievedContexts`, `success`/`errorMessage`, and `rawResponse`.
3. **Each `Evaluator`** returns an **`EvaluationResult`**: a score from 0.0 to 1.0, `passed`, `findings` (what failed) and `evidence` (what was checked). The generic evaluators so far are `MustContainEvaluator`, `MustNotContainEvaluator`, `LatencyEvaluator` and `CostEvaluator`.
4. **`ScoreAggregator`** turns a case's results into an **`AggregatedEvaluation`**. **`EvalAssert`** records it.
5. **After the suite, `EvalRunListener`** writes one run per test class to `test-output/ai-eval/{TestClass}/runs/{runId}/`. It contains:
   - `responses/`, `evaluations/`, `summary.json` and `summary.csv`
   - **`labels.csv`**: a sheet for hand-labelling
   - **`report.html`**: compared with that class's previous run

   It also prints the pass-rate gate verdict.

## Aggregation rules

A case passes only if all of these hold:
- the call succeeded (`EvalResponse.success`);
- at least one evaluator ran;
- no evaluator listed in `AggregationConfig.hardFailEvaluators` failed (the "never" checks; default `mustNotContain`);
- the weighted average of **every** evaluator's score reaches `ai.eval.minScore` (0.85), or `ai.eval.highRiskMinScore` (0.95) when `riskLevel` is `high`.

An evaluator with no entry in `weights` weighs 1.0, so a new evaluator always counts.

A failing case fails its own test, like any Jarvis test. The run's pass-rate gate (`ai.eval.minPassRate`) is reported, not enforced; a tolerance-based gate comes in phase 8 of the plan.

## Labels and calibration

Fill `humanLabel` (`pass`/`fail`) and `critique` in a run's `labels.csv`, then run:

```bash
mvn -q compile exec:java@calibration -Dexec.args="test-output/ai-eval/{TestClass}/runs/{runId}/labels.csv"
```

It prints TPR, TNR and kappa: how well the automated verdicts agree with yours.

## Properties

| Key | Where | Purpose |
|---|---|---|
| `ai.eval.minScore`, `ai.eval.highRiskMinScore`, `ai.eval.minPassRate` | `parameters/config.properties` | Case and run thresholds |
| `ai.eval.modelVersion`, `ai.eval.promptVersion`, `ai.eval.buildVersion` | `-D` on the command line | Stamped on the run, so runs can be compared |
| `aiteststudio.api.url`, `aiteststudio.maxLatencyMs`, `aiteststudio.maxCostUsd` | `parameters/staging-sg.properties` | Studio's address and budgets |
| `aiteststudio.username`, `aiteststudio.password` | `parameters/system.properties` (git-ignored) | Studio login; never committed |

## Adding an evaluator

1. Implement `Evaluator`. Return a score from 0.0 to 1.0, `passed`, a one-line `summary`, `findings` and `evidence`. `getName()` is its key in `AggregationConfig`.
2. Read only `EvalCase` and `EvalResponse`, never an app's raw format. If you need data the response doesn't carry, add a field to `EvalResponse` and have the adapter fill it.
3. Add tests in `src/test/java/automation/core/ai/`, like `EvaluatorsTest`.
4. If a failure must fail the case whatever its score, add the evaluator's name to `hardFailEvaluators`.

Add something to this layer only once a module uses it. App-specific logic stays in `automation.modules.{app}`.

## Run the tests

```bash
# Framework tests: offline
mvn test -Dtest='EvalCaseLoaderTest,EvaluatorsTest,ScoreAggregatorTest,EvalWiringTest,EvalRunListenerTest,LabelSheetTest,CalibrationTest,TalkToTestsMappingTest' -DbrowserName=api -DfailIfNoTests=false

# Talk to Tests eval: needs AI-Test-Studio running and its login in system.properties
mvn test -Dtest=TalkToTestsEvalTest -DbrowserName=api -DfailIfNoTests=false
```
