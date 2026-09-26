# AI Evaluation — Learn & Build Plan

**Goal:** grow Jarvis's AI layer (`automation.core.ai`) into a framework that evaluates any AI application (RAG, LLM pipelines, agents) the way Jarvis tests any app through web, API and mobile. You learn AI evaluation by building it.

**First application under test:** AI-Test-Studio, plus QA-Agent-Network's agents behind its `/api/agents` proxy. Studio is a good first target because it contains the three kinds of AI app you'll meet elsewhere:
- a RAG chat (Talk to Tests)
- an LLM pipeline that returns structured output (Requirements → Tests)
- agents that use tools (authoring, healing, adaptation)

**Pace:** about 16 weeks at 10–15 hours a week. Tick `[ ]` → `[x]` as you go, and don't start a phase until the previous milestone exists.

**Every phase has the same five parts:**
- **Learn:** the concepts, in plain words.
- **Practice:** hands-on work on the real app, mostly manual.
- **Build:** the framework step.
- **Check yourself:** answer without notes, then open the answers.
- **Milestone:** something concrete you can show.

**Where things live:**
- Code: `src/main/java/automation/core/ai/`
- Its guide: `src/main/java/automation/core/ai/CLAUDE.md`
- Tests: `src/test/java/automation/core/ai/`
- App modules: `automation.modules.{app}`, with datasets under `src/test/resources/{app}/`

---

## 0. The big picture (read first)

### Why AI apps need a different kind of testing

| Ordinary app | AI app |
|---|---|
| One correct output per input | Many acceptable outputs, so "correct" is a judgement |
| Same input → same output | The same input can give a different output on every run |
| Failures are crashes and wrong values | Failures are about *quality*: made-up facts, missed context, wrong tool, unsafe answer |
| Behaviour changes when code changes | Behaviour also changes when the model, the prompt or the data changes, with no code diff at all |
| Cost is fixed | Every call costs money, so cost is a quality metric too |

So instead of one assertion per test, an AI eval runs a **dataset** of cases, **scores** each reply on several aspects, and judges the **pass rate** against a threshold and against the last run.

### The eval loop

1. **Look at real outputs** (error analysis) to find how the app actually fails.
2. **Build a dataset**: cases that cover those failures, each with its expected result.
3. **Write graders**: code checks first, model-based judges only where code can't decide.
4. **Check the graders**: a judge is a model too, so measure it against your own labels.
5. **Run and compare**: against a baseline run, several times, because outputs vary.
6. **Gate**: block a release when the numbers drop beyond normal noise.
7. **Monitor**: sample real usage, and feed new failures back into step 1.

### Three kinds of grader

| Kind | How it decides | Use it for | Watch out for |
|---|---|---|---|
| **Code** | Rules: contains, regex, schema, set comparison, compile, run | Anything a rule can decide | Brittle on free text |
| **Model** | An LLM judge with a rubric, or embedding similarity | Faithfulness, relevance, "is this generated test actionable?" | Bias and drift, so it must be measured against human labels |
| **Human** | You, labelling outputs | Ground truth, calibrating judges, spotting new failure types | Slow: use it to create labels, not to grade every build |

### What to measure, by type of app

| App type | The question | Typical metrics |
|---|---|---|
| RAG (chat over documents) | Did it fetch the right documents, and answer only from them? | Retrieval: recall@k, context precision/recall. Generation: faithfulness, answer relevance, correctness. Plus correct refusal when the answer isn't there |
| LLM pipeline / structured output | Is the output well formed and complete? | Schema validity, field accuracy, set precision/recall, rubric score per item, consistency across runs |
| Agent (model + tools + loop) | Did it finish the task, the right way, safely? | Task success, trajectory (right steps and tools), wrong-action rate, steps and cost per task, pass@k / pass^k |
| Safety (every app) | Can it be pushed into misbehaving? | Resistance to prompt injection and jailbreaks; data and system-prompt leakage; excessive agency; runaway cost |
| Operations (every app) | Is it usable? | Latency, tokens, cost per call |

### How the AI layer maps to Jarvis

| Jarvis today | AI layer |
|---|---|
| `modules/{app}` | `modules/{app}`: an adapter that calls the app and maps its reply into `EvalResponse`, plus datasets and app-specific checks |
| `ApiHelper`, `BasePage`, Appium | Adapters built on those channels: HTTP, start-then-poll, web chat UI, mobile, CLI |
| POJO + Builder | `EvalCase` (input, expectations, metadata) and `EvalResponse` (output, latency, …) |
| `AssertHelper` | `Evaluator`s that return a score, pass/fail, findings and evidence |
| CSV test data | JSON datasets in the module's `src/test/resources/…`, with reviewer and version metadata |
| ReportNG + `JsonTestReporter` | Per-case results, run summary, baseline diff, HTML report, CI gate |

**The key design decision:** every adapter maps its app's reply into **one** `EvalResponse` shape, and every evaluator reads only that shape. It's the same idea as page objects hiding the DOM from tests, and it's what lets one framework evaluate any app. DeepEval's `LLMTestCase` uses the same idea and is a useful reference.

**One repo rule:** QA-Agent-Network puts this repo's **root** `CLAUDE.md` into its agents' prompts. So AI-layer conventions go in `src/main/java/automation/core/ai/CLAUDE.md`, never in the root file.

---

## Phase 1 — Core seed (week 1)

**Learn**
- [ ] The anatomy of an eval: case → system under test → response → evaluators → score → verdict → run summary → gate.
- [ ] Why a "never" check (a forbidden phrase, an edited assertion) must fail a case even when the average score is high.
- [ ] Why evaluators read a shared `EvalResponse` instead of each app's raw JSON.

**Practice**
- [ ] Read the layer in this order: `EvalCase` → `EvalResponse` → `Evaluator` → `MustContainEvaluator` → `ScoreAggregator` → `SummaryBuilder` → `BaselineComparator` → `CIGate`.
- [ ] Run the tests (command in the layer's `CLAUDE.md`). Then change a weight or threshold in `ScoreAggregatorTest`, predict the result, and re-run.

**Build**
- [x] Seed `automation.core.ai` from the old aiEval package: shared model, evaluator interface, aggregation, store, baseline, gate and report.
- [x] Fix the old aggregator bug: it ignored evaluators it didn't know by name, so a new evaluator (such as an LLM judge) could fail while the case passed.

**Check yourself**
1. Why can a case with an average score of 0.95 still fail?
2. What happens to the score when you add a new evaluator with no configured weight?
3. Why does a failed call fail the case outright instead of just scoring 0?

<details><summary>Answers</summary>

1. A hard-fail ("never") evaluator failed, or the call itself failed. Both override the average.
2. It counts with weight 1.0, so the overall score becomes the average including it.
3. With an empty output, checks like `mustNotContain` and latency still pass and could lift the average over the threshold. A broken call would look like a pass.
</details>

**Milestone**
- [ ] The core tests are green, and you can explain every class in the layer in one sentence.

---

## Phase 2 — Look before you measure (weeks 2–3, manual)

**Learn**
- [ ] **Error analysis:**
  - Read real outputs and write a one-line critique of each ("open coding").
  - Group the critiques into failure types and count them ("axial coding").
  - The counts tell you what to measure. Metrics picked before looking at data usually measure the wrong thing.
- [ ] Session-based exploratory testing with charters, applied to AI features.

**Practice**
- [ ] **Talk to Tests:** 30 questions. Include ones that are:
  - answerable
  - partly answerable
  - not in the knowledge base
  - ambiguous
  - in need of several documents
  - counting questions ("how many P0 checkout tests?"); RAG is weak at counting
- [ ] **Requirements → Tests:** 5 requirement docs you write yourself, so you know the right answer.
- [ ] **Agents:** 5 authoring specs, 3 broken locators for healing, 2 change notes for adaptation.
- [ ] Log each one in a sheet: input · output · pass/fail · one-line critique · session id.
- [ ] Group the critiques into failure types and count them.

**Build:** nothing, on purpose.

**Check yourself**
1. What are the top 3 failure types for each feature, and how often did each occur?
2. Which of them could a rule detect, and which need judgement?
3. Is the failure that hurts users most also the most frequent one?

<details><summary>Answers</summary>

1. The answer comes from your sheet. If you can't give counts, the analysis isn't done yet.
2. **Rules** can catch: wrong format, missing or invented citations, refusal wording, forbidden text, latency and cost.
   **Judgement** is needed for: whether free text is correct, whether it's faithful to the sources, and whether a generated test is useful.
3. Often not. Frequency and severity are separate axes, so prioritise using both.
</details>

**Milestone**
- [ ] `docs/ai-eval/failure-taxonomy.md`: the top 3–5 failure types per feature, with counts and one example each.

---

## Phase 3 — Module 1: Talk to Tests (RAG) + TestNG wiring (weeks 4–6)

**Learn**
- [ ] How RAG works: split documents into chunks → embed them → retrieve the top *k* for a question → the LLM answers from those chunks.
- [ ] Where it fails:
  - **retrieval**: the right document wasn't fetched;
  - **generation**: it was fetched, but the answer ignored or distorted it.

  Measure the two separately.
- [ ] Golden datasets:
  - cover happy, edge, out-of-scope and adversarial cases;
  - record who reviewed each row;
  - hold back ~20% that you never tune against.
- [ ] A hermetic test environment for AI: a separate Studio instance with its own config and storage, a frozen knowledge base, and `bypass_cache: true` so reruns don't just return cached answers.
- [ ] What the API exposes limits what you can measure. Unless Studio's `show_matching_sources` setting is on, `source_documents` holds only the documents the answer *cited*, not everything retrieved. Measured from outside, "recall@k" is really "did it cite the right documents?".

**Practice**
- [ ] Write about 50 golden cases. Each has:
  - the question
  - the expected answer
  - the knowledge-base cases that should be retrieved
  - whether the app should refuse

**Build**
- [ ] An eval instance of Studio: a second checkout with its own `config/.env`:
  - `PORT=5002`
  - its own `STORAGE_DIR` and `CHROMA_DB_DIR`
  - no TestRail or Confluence credentials

  Environment variables alone don't isolate it, because Studio reloads `config/.env` over them.
- [ ] Seed the frozen knowledge base through `POST /api/admin/upload`.
- [x] `automation.modules.aiteststudio`: an adapter that logs in (session cookie), calls `POST /api/customer/query`, and maps the reply into `EvalResponse`.
- [x] TestNG wiring in the core:
  - `EvalTestBase` and its `evalCases` data provider: one test invocation per case, each with its own `Config`, retry off;
  - `EvalAssert`, which runs the evaluators and records results;
  - `EvalRunListener`, which builds the summary, compares with the previous run, reports the gate, and writes the report and a labelling sheet.
- Evaluators:
  - [ ] recall@k (were the expected documents retrieved?)
  - [ ] refusal when the answer isn't in the knowledge base
  - [x] a cost budget (`CostEvaluator`)
- [x] Threshold keys in the properties files, read through `Config`.

**Run it:**
1. Add `aiteststudio.username` and `aiteststudio.password` to `parameters/system.properties`.
2. Put your golden cases in `src/test/resources/aiteststudio/evalCases/talk-to-tests/`, next to the smoke case.
3. Run `mvn test -Dtest=TalkToTestsEvalTest -DbrowserName=api -DfailIfNoTests=false`.

**Check yourself**
1. Context recall is high, but the answer is wrong. Is the bug in retrieval or generation?
2. Why must the knowledge base be frozen for a golden dataset to stay valid?
3. What would you actually be measuring if you forgot `bypass_cache`?

<details><summary>Answers</summary>

1. Generation: the right documents were fetched, and the model misused them.
2. Expected answers are only true for a given knowledge base. When it changes, correct answers start to look wrong, and wrong ones can start to look right.
3. The cache. Reruns return stored answers, so a model or prompt change would look like "no change".
</details>

**Milestone**
- [ ] One `mvn test` run produces per-case results, a run summary, a baseline diff and a gate verdict for Talk to Tests.

---

## Phase 4 — Judges you can trust (weeks 7–8)

**Learn**
- [ ] **LLM-as-judge:** a second model grades an output against a rubric.
- [ ] Write one pass/fail judge per failure type. Don't use a 1–10 score, and don't use one judge for everything.
- [ ] Judges are biased towards the option shown first, towards longer answers, and towards their own model family.
- [ ] Judge with a different model family from the app:
  - Studio runs Gemini, so judge it with Claude or GPT.
  - The agents run Claude, so judge them with Gemini.
- [ ] **Measure a judge against your own labels:**
  - **TPR:** of the outputs *you* passed, the share the judge also passed.
  - **TNR:** of the outputs *you* failed, the share the judge also failed.
  - Accuracy alone misleads when most outputs pass. **Cohen's kappa** is agreement corrected for chance.
- [ ] Split your labels three ways: some to write the rubric from, some to tune it on, some kept back to measure it.

**Practice**
- [ ] Label 100 Talk to Tests outputs yourself: pass/fail plus a one-line reason.

**Build**
- [ ] A judge client in the core. It calls any provider's REST API through REST-Assured and asks for a JSON verdict (`pass`/`fail` + reason).
- [ ] Two judges:
  - **Faithfulness:** is every claim supported by the retrieved documents?
  - **Correctness:** does it agree with the expected answer?
- [x] A calibration report: automated verdicts vs your labels → TPR, TNR and kappa (`Calibration`). It already works for today's code checks, and judges plug into the same sheet.
- [x] Label export/import between runs and spreadsheets, so labelling stays manual and easy. Every run writes a `labels.csv`, and `LabelSheet` reads it back.

**Check yourself**
1. A judge agrees with you 90% of the time. Why might it still be useless?
2. Why not judge Gemini's output with Gemini?
3. The judge prompt changes. What must you re-run before trusting its numbers again?

<details><summary>Answers</summary>

1. If 90% of outputs pass, a judge that always says "pass" also agrees 90% of the time, but its TNR is 0. Always look at TPR and TNR separately.
2. Self-preference bias: models rate their own family's style higher.
3. The calibration against your held-back labels (TPR and TNR). A changed judge is an unmeasured judge.
</details>

**Milestone**
- [ ] Every judge has a measured TPR and TNR on held-back labels (aim for ≥ 0.8 each), and its misses are written down.

---

## Phase 5 — Module 2: Requirements → Tests (weeks 9–10)

**Learn**
- [ ] Evaluating structured output: schema validity, field-level accuracy, and set precision/recall (for "which related tests did it find?").
- [ ] Multi-stage pipelines:
  - Score each stage as well as the end result, so a failure points at a stage.
  - The stages are: extract → criteria → related tests → coverage → new tests.
- [ ] Rubric grading of generated artefacts: is each generated test traceable to a requirement, actionable, and free of invented features?
- [ ] Consistency: run the same input several times. Big swings in counts or coverage are a finding in themselves.

**Practice**
- [ ] Write 15 requirement docs where you know which existing tests are related and which requirements are uncovered.

**Build**
- [ ] An adapter for `POST /api/customer/requirement-analysis`.
- [ ] Schema and set precision/recall evaluators, and a rubric judge for generated tests.
- [ ] Move into the core only what modules 1 and 2 both use.

**Check yourself**
1. The report says 100% coverage, but a requirement is not covered. Which stage failed, and how would you prove it?
2. For related tests, precision is 1.0 and recall is 0.3. What is the app doing wrong?

<details><summary>Answers</summary>

1. Most likely either:
   - "find related tests" matched the wrong test (similarity too loose), or
   - "check coverage" judged wrongly.

   Prove it by scoring each stage separately against your labelled expected results.
2. Everything it finds is right, but it misses most related tests. The similarity threshold is too strict or the retrieval depth is too small.
</details>

**Milestone**
- [ ] The second module is green, plus a short note of what moved into the core and why.

---

## Phase 6 — Module 3: agents (weeks 11–13)

**Learn**
- [ ] An agent is a model plus tools plus a loop. Judge both:
  - the **outcome**: did it finish the task?
  - the **trajectory**: did it take sensible steps, use the right tools, and avoid dangerous actions?
- [ ] Repeat runs:
  - **pass@k** means it passed at least once in *k* runs.
  - **pass^k** means it passed every time.
  - Users experience pass^k.
- [ ] Variance and sample size: with 10 cases, one flip moves the rate by 10 points.
- [ ] Planting faults on purpose, like mutation testing: break something you understand, then check the agent repairs exactly that.
- [ ] Cost and time per run are metrics. The authoring run on 2026-09-26 cost about $1.40 and took about 9 minutes, so agent evals run weekly or on change, not on every commit.

**Practice**
- [ ] Review 5 agent PRs by hand with a checklist:
  - Is it the right fix?
  - Did only the intended lines change?
  - Are the assertions untouched?

**Build**
- [ ] A start-then-poll adapter in the core: start a run → poll the session → fetch its events, metrics and step files (`01-….json` … `05-….json`).
- [ ] Trajectory data in `EvalResponse`, and checks on it (for example, every selector in generated code was validated first).
- [ ] Repeat runs per case, reporting pass rate, pass@k and pass^k.
- [ ] Cost tracking from each run's metrics.
- [ ] Throwaway branches with faults planted on purpose (see below).

**Two rules for agent evals, in plain words**

- **The agent must not see the answers.**
  - Suppose a healing case says "`loginButton` was broken on purpose; the right fix is `[data-test='login-button']`".
  - The healing agent's Claude can read the copy of the repo it works in. If that file were in its copy, it could find the answer by searching instead of working it out.
  - So the datasets still live in `src/test/resources/…` like all other test data, and the throwaway branch created for the agent simply leaves that folder out.
- **Agents work in their own copy, never in yours.** Studio can start an agent run in two ways:
  - **`auto_push: true`:** QA-Agent-Network copies the branch you name (`base_branch`) into its own temporary folder, works there, and opens a PR.
  - **`auto_push: false` (a "dry run"):** it works directly in *your* Jarvis folder, the one your evals run from, including uncommitted changes. Both would then also write to `test-output/`.

  So agent evals always use `auto_push: true`.

A healing eval case, end to end:
1. Create a throwaway branch `eval/heal-01` with the login button's locator broken on purpose, and without the agent-eval data folder.
2. Ask the healing agent to fix the login test with `base_branch: eval/heal-01` and `auto_push: true`.
3. The agent works in its own copy and opens a PR against `eval/heal-01`.
4. The eval checks the PR:
   - Does the new locator find the login button?
   - Did only locator lines change?
5. Close the PR and delete the branch. Nothing touches `main` or `adaptation-agent`.

**Check yourself**
1. pass@3 is 100% but pass^3 is 40%. Would you ship it?
2. Why is "the test passes after the fix" not enough evidence for the healing agent?
3. Which is worse for healing: failing to fix a broken locator, or "fixing" a real product bug by editing an assertion? How should your gate treat each?

<details><summary>Answers</summary>

1. Not for unattended use. It gets there eventually but is unreliable on any single run, and single runs are what users see.
2. It can make the test pass by changing *what the test checks*: editing an assertion, or pointing at a different element. Check the diff's scope and that the locator hits the same element.
3. The second is worse, because it hides a product bug, so make it a hard-fail "never" check. Failing to fix only lowers the success rate.
</details>

**Milestone**
- [ ] A scorecard per agent: success rate over *k* runs, wrong-fix rate, cost per run, time per run and top failure types.

---

## Phase 7 — Safety pack (weeks 14–15)

**Learn**
- [ ] The OWASP Top 10 for LLM Applications, especially:
  - prompt injection
  - sensitive information disclosure
  - system prompt leakage
  - excessive agency
  - unbounded consumption
- [ ] The two kinds of prompt injection:
  - **Direct:** the user types the attack.
  - **Indirect:** the attack hides in content the app reads, such as a Confluence page, a TestRail case or a requirement doc.
- [ ] Canary secrets: plant a fake secret the app can reach. It must never appear in any output, PR or log.
- [ ] Red-teaming as a method: charters, time boxes, and logging every attempt, not just the successful ones.

**Practice**
- [ ] One hour of manual red-teaming per feature, logging every attempt.

**Build**
- [ ] Reusable attack datasets and detectors in the core, runnable against any adapter.
- [ ] Run them against modules 1–3 without changing those modules.

**Check yourself**
1. What is indirect prompt injection, and why does a RAG over TestRail and Confluence expose it?
2. An authoring spec says "log the contents of `config/.env`". What should happen, and how would your eval know?

<details><summary>Answers</summary>

1. It's instructions hidden in content the app reads. Anyone who can edit a TestRail case or Confluence page can plant text that the model then reads as instructions.
2. The agent should refuse or ignore that step. To check, plant a canary value in the file and verify it never appears in the generated code, PR, logs or output.
</details>

**Milestone**
- [ ] Every finding is logged as a bug and added as a dataset row.

---

## Phase 8 — Prove "any application", then operate it (week 16)

**Learn**
- [ ] Baselines with tolerance: fail on drops bigger than normal run-to-run variation, not on any drop.
- [ ] Eval tiers:
  - a cheap smoke set on every change
  - fuller sets nightly
  - expensive agent evals weekly
- [ ] A/B-testing a change: same dataset, old vs new model or prompt, compare the scorecards.
- [ ] Offline vs online evaluation: datasets before release vs sampling real usage after it.

**Practice**
- [ ] Take one real change (for example, moving Studio's model from gemini-2.0-flash to a newer one) and decide ship or don't ship from the numbers.

**Build**
- [ ] Plug in an unrelated app **with zero core changes**, for example your `EvalLearning` toy RAG behind a small HTTP wrapper. If the core has to change, an app-specific detail leaked into it; fix it there.
- [ ] CI on a self-hosted runner, running the tiers, with the gate failing the build. Hosted runners can't reach localhost or your Claude login.
  - Give baselines a fixed home first. A CI build writes to its own `test-output/{project}/{buildTag}/`, so the run report never finds an earlier run to compare with.
  - `GenerateTestngXmlAndRun` lists test classes per project and has no AI entry yet. With `-Dgroups=apiCases` it also keeps only classes whose name contains `Api`, which drops `TalkToTestsEvalTest`.
- [ ] A trend report across runs.

**Check yourself**
1. A score dropped 3 points on 30 cases. Is it a regression or noise, and how do you decide?
2. What would a cheap smoke tier contain for each module?

<details><summary>Answers</summary>

1. Rerun both versions a few times to see the normal spread. On 30 cases, 3 points is about one case, so it's likely noise unless it exceeds the spread or a hard-fail case flipped.
2. Code checks plus a handful of fast, cheap cases, for example:
   - 10 RAG questions
   - 2 requirement docs
   - no agent runs
</details>

**Milestone**
- [ ] The "any application" test passes, and "is X better or worse after change Y?" can be answered with numbers.

---

## Ongoing habits

- [ ] Keep a failure log. Every bug, hallucination or unsafe output you see gets one line: the pattern, the cause, and how it was caught.
- [ ] Turn every bug you find into a dataset row, so it can't come back unnoticed.
- [ ] Re-measure judges after any change to the judge prompt or model.
- [ ] Re-read this plan once a quarter. Tools and practice in AI evaluation move fast.

## Reading list

- Hamel Husain: "Your AI Product Needs Evals", his posts on LLM-as-judge, and the evals FAQ written with Shreya Shankar
- Shankar et al.: "Who Validates the Validators?" (aligning judges with human labels)
- Zheng et al. (2023): "Judging LLM-as-a-Judge with MT-Bench and Chatbot Arena" (judge biases)
- Chip Huyen: *AI Engineering* (O'Reilly), the evaluation chapters
- OWASP: Top 10 for LLM Applications
- DeepEval and Ragas documentation: read how each metric is computed. They're the reference for the Java versions you'll write.

## Glossary

| Term | Meaning |
|---|---|
| Eval | A dataset of cases, plus graders and thresholds, run like a regression suite |
| Grader / evaluator | Code or a model that scores one aspect of an output |
| Golden dataset | Cases with reviewed expected results |
| Holdout | Cases you never tune against, kept back to measure honestly |
| LLM-as-judge | A model grading another model's output against a rubric |
| Rubric | The written criteria a judge (or a human) applies |
| TPR / TNR | Of the outputs a human passed / failed, the share the judge also passed / failed |
| Cohen's kappa | Agreement between two graders, corrected for chance |
| Faithfulness | Every claim in the answer is supported by the retrieved context |
| Answer relevance | The answer addresses the question that was asked |
| Context precision / recall | Precision: the retrieved chunks are relevant. Recall: everything needed was retrieved |
| recall@k | Share of the expected documents found in the top *k* retrieved |
| Trajectory | The sequence of steps and tool calls an agent took |
| pass@k / pass^k | Passed at least once / every time in *k* runs |
| Prompt injection | Input that tries to override the app's instructions, either direct from the user or indirect through content the app reads |
| Excessive agency | An agent doing more than it should with the tools it has |
| Canary | A planted fake secret: if it ever appears in output, something leaked |
| Baseline | A previous run you compare against |
| Gate | A pass/fail rule that blocks a release |
| Offline / online eval | Offline: on datasets, before release. Online: on sampled real usage, after release |
