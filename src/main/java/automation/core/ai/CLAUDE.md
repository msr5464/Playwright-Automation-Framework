# AI evaluation layer — `automation.core.ai`

Evaluates AI applications (RAG, LLM pipelines, agents) the way the web, API and mobile layers test ordinary apps. The phased learn-and-build plan is [docs/ai-eval/PLAN.md](../../../../../../docs/ai-eval/PLAN.md).

This guide lives here, not in the root `CLAUDE.md`, because QA-Agent-Network puts the root file into its agents' prompts. Keep AI-layer conventions in this file.

## How one case flows

1. **`EvalCase`**: one dataset row (`input`, `mustContain`, `mustNotContain`, `riskLevel`, `tags`, `metadata`), loaded from `.json` files by `EvalCaseLoader`.
2. **The module's adapter** (in `automation.modules.{app}`) calls the app and returns an **`EvalResponse`**, the one shape every evaluator reads.
3. **Each `Evaluator`** scores the case and response, producing an **`EvaluationResult`** with a score from 0.0 to 1.0, `passed`, `findings` (what failed) and `evidence` (what was checked). The generic evaluators so far are `MustContainEvaluator`, `MustNotContainEvaluator` and `LatencyEvaluator`.
4. **`ScoreAggregator`** turns a case's results into an **`AggregatedEvaluation`**: pass/fail, overall score and top issues.
5. **After all cases:**
   - `SummaryBuilder` builds the `RunSummary`.
   - `ResultStore` saves JSON and CSV under `{outputDir}/runs/{runId}/`.
   - `BaselineComparator` diffs the run against an earlier one.
   - `CIGate` fails the run below a pass rate.
   - `HtmlReportGenerator` writes the report.

## Aggregation rules

A case passes only if all of these hold:
- the call succeeded (`EvalResponse.success`);
- at least one evaluator ran;
- no evaluator listed in `AggregationConfig.hardFailEvaluators` failed (the "never" checks; default `mustNotContain`);
- the weighted average of **every** evaluator's score reaches `defaultMinScore` (0.85), or `highRiskMinScore` (0.95) when `riskLevel` is `high`.

An evaluator with no entry in `weights` weighs 1.0, so a new evaluator always counts.

## Adding an evaluator

1. Implement `Evaluator`. Return a score from 0.0 to 1.0, `passed`, a one-line `summary`, `findings` and `evidence`. `getName()` is its key in `AggregationConfig`.
2. Read only `EvalCase` and `EvalResponse`, never an app's raw format. If you need data the response doesn't carry, add a field to `EvalResponse` and have the adapter fill it.
3. Add tests in `src/test/java/automation/core/ai/`, like `EvaluatorsTest`.
4. If a failure must fail the case whatever its score, add the evaluator's name to `hardFailEvaluators`.

Add something to this layer only once a module uses it. App-specific logic stays in `automation.modules.{app}`.

## Not built yet

The TestNG wiring (data provider, `EvalAssert`, suite listener), app adapters, LLM judges and agent support each arrive in their own phase of the plan.

## Run the tests

```bash
mvn test -Dtest='ScoreAggregatorTest,EvaluatorsTest,EvalCaseLoaderTest' -DbrowserName=api -DfailIfNoTests=false
```
