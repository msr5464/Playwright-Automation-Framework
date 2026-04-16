# Jarvis — AI Agent Guide

This is the single source of truth for AI agents working in this repo. Read it fully before writing or running any code.

---

## Framework Overview

| Layer      | Technology              |
|------------|-------------------------|
| Language   | Java 21                 |
| Build      | Maven 3.8+              |
| Test Runner| TestNG 7.9.0            |
| Web UI     | Playwright 1.54.0       |
| API        | REST-Assured 5.3.2      |
| Mobile     | Appium 9.3.0            |
| Reporting  | ReportNG + JsonTestReporter |

Source layout:
- `src/main/java/automation/core/` — framework internals (do not modify unless working on the framework itself)
- `src/main/java/automation/modules/` — feature helpers, POJOs, builders, page objects
- `src/test/java/automation/` — test classes
- `src/main/java/automation/aiEval/` — separate AI evaluation subsystem; **do not touch unless explicitly asked**

---

## Reporting — what exists, what does not

**Active reporters:**
- **ReportNG** — HTML report at `{resultsDirectory}/html/index.html`
- **JsonTestReporter** (`automation.core.JsonTestReporter`) — machine-readable JSON at `{resultsDirectory}/report.json`

---

## How to Run Tests

### Compile only (no tests) — use this first to catch compile errors before running
```bash
mvn compile -q
```

### Run a single test method — use this to self-test any code you write
```bash
mvn test -Dtest=GitHubApiTest#getPublicUserInfo -DfailIfNoTests=false
```

### Run an entire test class
```bash
mvn test -Dtest=GitHubApiTest -DfailIfNoTests=false
```

### Run via static testng.xml (runs GitHub + SauceDemo + AI eval tests)
```bash
mvn test
```

### Run programmatic suite (GenerateTestngXmlAndRun) by project
```bash
mvn test -DprojectName=GitHub -Denvironment=staging -DbrowserName=chromium -Dgroups=regression
mvn test -DprojectName=SauceDemo -Denvironment=staging -DbrowserName=chromium -Dgroups=regression
mvn test -DprojectName=fullsuite -Denvironment=staging -DbrowserName=chromium -Dgroups=regression
```

### Key system properties

| Property        | Values                                    | Default        |
|-----------------|-------------------------------------------|----------------|
| `projectName`   | GitHub, SauceDemo, fullsuite              | CustomerFrontend |
| `environment`   | staging, qa-1, demo                       | staging        |
| `browserName`   | chromium, firefox, webkit, api            | chromium       |
| `groups`        | regression, smokeTest, apiCases, webCases | regression     |
| `country`       | SG, HK, US, AU, ID, VN                   | sg             |
| `headless`      | true, false                               | false          |

---

## Self-Testing Workflow (MANDATORY for AI agents)

Every time you write or modify code, follow this exact sequence before declaring the task done. Do not skip steps.

### Step 1 — Compile
```bash
mvn compile -q 2>&1 | head -50
```
Fix every compile error before proceeding. Do not run tests against broken code.

### Step 2 — Run the specific test(s) affected by your change
```bash
# API test (no browser needed, fast)
mvn test -Dtest=GitHubApiTest -DfailIfNoTests=false 2>&1 | tail -30

# Web test
mvn test -Dtest=SauceDemoWebTest#loginAndVerifyProductsPage -DfailIfNoTests=false 2>&1 | tail -30
```

Use the narrowest scope possible — single method if you touched one test, single class if you touched a helper.

### Step 3 — Read the JSON report
```bash
cat test-output/report.json
```
The JSON report is the authoritative result source for AI agents. Check every entry:
- `"status": "FAILED"` → read `failureMessage` and `failureLocation`, fix the issue, go back to Step 1
- `"status": "PASSED"` → proceed

### Step 4 — Check for compile warnings on test classes
```bash
mvn test-compile -q 2>&1 | grep -i "warning\|error"
```

### Step 5 — Run the full relevant class to catch regressions
```bash
mvn test -Dtest=GitHubApiTest -DfailIfNoTests=false
```
All tests in the class must pass before the task is done.

### Reading failures
The JSON report format:
```json
{
  "testName": "getPublicUserInfo",
  "className": "automation.github.GitHubApiTest",
  "status": "FAILED",
  "failureMessage": "Expected [octocat] but got [null]",
  "failureLocation": "GitHubApiTest.java:36",
  "durationMs": 1200,
  "screenshotPath": ""
}
```
`failureLocation` gives you the exact file and line. Go there first.

---

## Existing Test Classes (working reference)

| Class | Location | Type | Notes |
|-------|----------|------|-------|
| `GitHubApiTest` | `src/test/java/automation/github/` | API | Public GitHub API, no auth needed — best for self-testing |
| `GitHubLoginTest` | `src/test/java/automation/github/` | Web | Requires GitHub credentials in config |
| `SauceDemoApiTest` | `src/test/java/automation/saucedemo/` | API | JSONPlaceholder public API |
| `SauceDemoWebTest` | `src/test/java/automation/saucedemo/` | Web | SauceDemo public test site |
| `TestDemo` | `src/test/java/automation/` | Utility | Local Jenkins simulation — not a real test |

**`GitHubApiTest` is the best class to run for a quick self-test** — it hits a public API, needs no credentials, no browser, and runs in seconds.

---

## How to Write a Test

### Minimal structure
```java
package automation.{feature};

import automation.core.*;
import automation.core.Enums.*;
import org.testng.annotations.Test;

public class MyFeatureTest extends TestBase {

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API},
          description = "Fetch user info and verify login field is present")
    @TestVariables(automatedBy = QA.Mukesh)
    public void fetchUserInfo(Config config) {
        MyFeatureHelper helper = new MyFeatureHelper(config);

        MyFeatureData result = helper.execute(MyFeatureApi.GetItem.withPath("id", "1"), MyFeatureData.class);

        AssertHelper.assertNotNull(config, result.getId(), "ID should be present");
        AssertHelper.assertEquals(config, result.getName(), "expected", "Name should match");
    }
}
```

### Mandatory rules
- Always `extends TestBase`
- Always `dataProvider = "getConfig"` (or `"getTwoConfigs"` / `"getMultipleConfigs"` for multi-actor)
- Always annotate with `@TestVariables(automatedBy = QA.Mukesh)` — add `testrailData = "suiteId:caseId:type"` only when a TestRail case exists
- Always use `AssertHelper` for assertions — never `Assert.*` directly
- Always pass `config` to every constructor and helper method
- Never call `allocateUser()` unless the test requires a DB-backed user pool (the GitHub and SauceDemo tests do not use it — they read credentials from CSV or config)

### Test groups (use constants from TestBase)
| Constant | String | Use |
|----------|--------|-----|
| `GROUP_REGRESSION` | `"regression"` | All standard tests |
| `GROUP_API` | `"apiCases"` | API-only tests |
| `GROUP_WEB` | `"webCases"` | Browser UI tests |
| `GROUP_SMOKE` | `"smokeTest"` | Smoke subset |
| `GROUP_CRITICAL` | `"criticalFlows"` | Business-critical |
| `GROUP_PROD_SANITY` | `"prodSanity"` | Production smoke |
| `GROUP_ANDROID` | `"androidCases"` | Android mobile |
| `GROUP_IOS` | `"iosCases"` | iOS mobile |

Always include at least `GROUP_REGRESSION` plus one of `GROUP_API` or `GROUP_WEB`.

---

## How to Add a New Feature Module

Follow this exact structure. Use `automation.modules.github` as the live reference implementation.

```
src/main/java/automation/modules/{feature}/
├── {Feature}Data.java          # POJO — @Data @NoArgsConstructor @AllArgsConstructor + @JsonProperty
├── {Feature}Builder.java       # Fluent builder — .with*() methods + withDefaults() + build()
├── {Feature}Helper.java        # Extends ApiHelper (external APIs) — orchestration methods
├── api/
│   └── {Feature}Api.java       # Enum implementing ApiDetails — one entry per endpoint
└── web/
    └── {Page}Page.java         # Extends BasePage — locators + actions for ONE page only

src/test/java/automation/{feature}/
└── {Feature}Test.java          # Extends TestBase — one @Test method per scenario
```

### Data POJO
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class WidgetData {
    @JsonProperty("widget_name") private String widgetName;
    @JsonProperty("widget_type") private String widgetType;
    @JsonProperty("id")          private String id;       // response-only
    @JsonProperty("status")      private String status;   // response-only
}
```

### Builder
```java
public class WidgetBuilder {
    private String widgetName;
    private String widgetType = "Standard"; // default

    public WidgetBuilder withWidgetName(String name) { this.widgetName = name; return this; }
    public WidgetBuilder withWidgetType(String type) { this.widgetType = type; return this; }

    public WidgetBuilder withDefaults() {
        if (widgetName == null) widgetName = "Widget_" + DataGenerator.randomAlphaString(5);
        return this;
    }

    public WidgetData build() {
        withDefaults();
        WidgetData w = new WidgetData();
        w.setWidgetName(widgetName);
        w.setWidgetType(widgetType);
        return w;
    }
}
```

### API enum
```java
public enum WidgetApi implements ApiDetails {
    CreateWidget(Method.POST,   "/v1/widgets",      201),
    GetWidget(   Method.GET,    "/v1/widgets/{id}", 200),
    DeleteWidget(Method.DELETE, "/v1/widgets/{id}", 200);

    private final Method method;
    private final String endpoint;
    private final int expectedStatus;

    WidgetApi(Method method, String endpoint, int expectedStatus) {
        this.method = method; this.endpoint = endpoint; this.expectedStatus = expectedStatus;
    }

    @Override public Method getMethod()       { return method; }
    @Override public String getEndpoint()     { return endpoint; }
    @Override public int getExpectedStatus()  { return expectedStatus; }

    public PathBuilder withPath(String param, String value) {
        return new PathBuilder(this.method, this.endpoint, this.expectedStatus).withPath(param, value);
    }
}
```

### Helper (external/3rd-party API — extends ApiHelper directly)
```java
public class WidgetHelper extends ApiHelper {
    private static final String BASE_URL = "https://api.widget.io";

    public WidgetHelper(Config config) {
        super(config, BASE_URL);
    }

    public WidgetHelper(Config config, String authToken) {
        this(config);
        if (authToken != null) setAuthToken(authToken);
    }
}
```

### Page object
```java
public class WidgetListPage extends BasePage {
    private final Locator createButton = page.locator("[data-cy='create-widget-btn']");

    public WidgetListPage(Config config) {
        super(config);
        assertPageLoaded(createButton);                     // single element
        // assertPageLoaded(createButton, alternativeBtn); // any one visible = page loaded
    }

    public WidgetDetailPage clickCreate() {
        click(createButton, "Create Widget button");
        return new WidgetDetailPage(config);  // always return the next page
    }
}
```

---

## API Tests

### Execute with typed response (happy path)
```java
// No body
GitHubData user = github.execute(GitHubApi.GetUser.withPath("username", "octocat"), GitHubData.class);

// With body
WidgetData created = helper.execute(WidgetApi.CreateWidget, widgetBody, WidgetData.class);
```

### Execute for side-effect only (delete, follow, etc.)
```java
helper.execute(WidgetApi.DeleteWidget.withPath("id", widgetId));
```

### Negative tests — raw response
```java
Response response = github.executeRaw(GitHubApi.GetUser.withPath("username", "nonexistent_xyz"), null);
AssertHelper.assertEquals(config, response.getStatusCode(), 404, "Non-existent user should return 404");
```

### Dynamic payload without a POJO
```java
Response response = helper.executeRaw(WidgetApi.CreateWidget,
    helper.map().put("widget_name", "Test").put("invalid_field", true).build());
AssertHelper.assertEquals(config, response.getStatusCode(), 400, "Should reject invalid payload");
```

### Path parameters
```java
// Single param
GitHubApi.GetUser.withPath("username", "octocat")

// Multiple params (chainable)
GitHubApi.GetRepository.withPath("owner", "torvalds").withPath("repo", "linux")
```

---

## Assertions

Always use `AssertHelper`. Never use `Assert.*` directly.

```java
AssertHelper.assertNotNull(config, user.getId(), "User ID should be present");
AssertHelper.assertEquals(config, user.getLogin(), "octocat", "Login should match");
AssertHelper.assertTrue(config, user.getPublicRepos() >= 0, "Repos count should be non-negative");
AssertHelper.assertContains(config, response.getBody().asString(), "octocat", "Body should contain username");

// Element assertions (Web UI only)
AssertHelper.assertElementVisible(config, locator, "Submit button");
AssertHelper.assertElementText(config, locator, "Expected Text", "Page title");
```

Assertions are soft by default — execution continues after a failure, and the test is marked FAILED at the end. To hard-stop immediately on failure use `config.logFailToEndExecution("message")`.

---

## Logging

Use `config.log*()` instance methods. They write to ReportNG, the JSON report, and the console.

```java
config.logStep("Login to GitHub and verify dashboard loads");    // test classes ONLY
config.logComment("Clicking create repository button");           // helpers and page objects
config.logPass("Repository created with correct name");          // intermediate step confirmations only
config.logFail("Repository not found in list");                  // red + screenshot
config.logWarning("Optional banner not present, continuing");    // non-blocking issues
```

Rules:
- `config.logStep()` → **test class methods only**
- `config.logComment()` → helpers and page objects
- Do **NOT** add `config.logPass()` as the last line of a test — the framework logs PASS/FAIL automatically after each test

---

## Element Interactions

**In page objects** (extend `BasePage`) — use inherited methods, no `config` arg needed:

| Action | Method |
|--------|--------|
| Click | `click(locator, "Button name")` |
| Fill / type | `fillText(locator, text, "Field name")` |
| Type char-by-char | `typeText(locator, text, "Field name")` |
| Get text | `getText(locator, "Label name")` |
| Get input value | `getInputValue(locator, "Field name")` |
| Check checkbox | `check(locator, "Checkbox name")` |
| Select dropdown | `selectOption(locator, value, "Dropdown name")` |
| Hover | `hover(locator, "Element name")` |
| Scroll to | `scrollToElement(locator, "Element name")` |
| Is visible | `isElementDisplayed(locator)` |
| JS click (fallback) | `clickViaJS(locator, "Button name")` |

**In helpers** (do not extend `BasePage`) — use `Element` static methods:

| Action | Method |
|--------|--------|
| Click | `Element.click(config, locator, "Button name")` |
| Fill | `Element.enterData(config, locator, text, "Field name")` |
| Get text | `Element.getText(config, locator, "Label name")` |
| Is visible | `Element.isElementDisplayed(config, locator, "name")` |

Never call Playwright locator methods directly (`locator.click()`, `locator.fill()`).

---

## WaitHelper

Never use `Thread.sleep()`. Use `WaitHelper`:

| Method | When to use |
|--------|-------------|
| `waitForElementToBeVisible(config, locator, name)` | Standard — element appears |
| `waitForElementToBeHidden(config, locator, name)` | Spinners, toasts to disappear |
| `waitForOptionalElementToBeVisible(config, locator, name)` | Conditional elements — 5s, returns boolean |
| `waitForElementToBeAttached(config, locator, name)` | In DOM but not yet visible |
| `waitForElementToBeDetached(config, locator, name)` | Wait for removal from DOM |
| `waitForAnyElementToBeDisplayed(config, locators...)` | Poll until any one of multiple locators is visible |
| `waitForNetworkIdle(config)` | After form submissions with no visible feedback |

Rule: use `assertPageLoaded(locator)` (inherited from `BasePage`) **at the end of every page constructor** — it waits for the element and hard-fails the test if the page did not load. Use `waitForElementToBeVisible` everywhere else.

---

## Configuration System

Config loads in this order (later overrides earlier):

1. `parameters/config.properties` — base defaults
2. `parameters/{environment}/config.properties` — env subdirectory
3. `parameters/{environment}-{country}.properties` — env + country
4. `parameters/{environment}/{environment}-{country}.properties` — env subdirectory + country
5. `parameters/system.properties` — local developer secrets (git-ignored, never commit)
6. `-D` system properties — highest priority

Access in code:
```java
config.getRunTimeProperty("github.token")   // any runtime property
Config.environment                           // static globals — use these, not getRunTimeProperty
Config.browserName
Config.country
Config.projectName
```

Current `parameters/config.properties` defaults:
```
environment=staging
browserName=chromium
headless=false
country=sg
ObjectWaitTime=30       # element wait timeout in seconds
VideoMode=on_failure    # OFF | ON | ON_FAILURE
endExecutionOnFailure=false
```

---

## Test Data

### Builder (primary pattern — use this by default)
```java
WidgetData widget = new WidgetBuilder()
    .withWidgetName("Marketing Widget")
    .withWidgetType("Premium")
    .build();
```

### DataGenerator for random values
```java
DataGenerator.randomAlphaString(8)       // random 8-char alpha
DataGenerator.randomAlphaNumericString(10) // random alphanumeric
DataGenerator.randomEmail()               // random email
DataGenerator.randomFullName()            // random full name
DataGenerator.randomNumber(10, 100)       // random int in range
DataGenerator.getCurrentDateTime("dd-MM-yyyy HH:mm:ss")
```

### CSV (only when data is reused across multiple flows)
```java
// Load one row matched by column value
Map<String, String> creds = TestDataReader.loadCsvRowByColumnValue(
    "github",           // module folder under src/test/resources/
    "github-users",     // CSV filename without .csv
    "role",             // column to match
    "admin",            // value to match
    Config.environment  // optional second filter column
);
String username = creds.get("username");
```

CSV files: `src/test/resources/{feature}/csvFiles/{name}.csv`

Supported placeholders in CSV cells:
- `{randomString:8}` — 8-char random alphanumeric
- `{randomEmail}` — random email
- `{randomNumber:4}` — 4-digit random number

---

## User Allocation (DB-backed user pool — not used in GitHub/SauceDemo tests)

Only use this for internal applications that manage users in a database.

```java
// Single user
User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

// Two users
// dataProvider = "getTwoConfigs" — method receives (Config config1, Config config2)
User admin    = allocateUser(config1, UserType.Admin,    Feature.CARD, Country.SG);
User employee = allocateUser(config2, UserType.Employee, Feature.CARD, Country.SG);
```

Users are automatically released by `@AfterMethod`. Do not release manually.

---

## Coding Rules

### Naming
- Full descriptive names: `merchantName` not `mName`, `orderId` not `id`
- Methods describe the action: `addProductToCart()`, `verifyRepositoryMetadata()`
- Enum values in CamelCase: `SpringGreen`, `BerryBlue` — not `SPRING_GREEN`

### Page objects
- One class = one page. No cross-page locators.
- Locator priority: `[data-cy='...']` > `#id` > `[name='...']` > css > xpath
- XPath: use `contains()` only — never exact text match, positional selectors, or deep nesting
- Navigation methods must return the next page object
- Call `assertPageLoaded(locator)` at the end of every constructor — no `waitUntilLoaded()` override needed

### Helpers
- A Helper method orchestrates ≥2 page objects. Single-page chains go in the page class.
- Do not instantiate page objects in test classes — use the Helper

### Test classes
- One user per test — never share accounts between test methods
- `logStep()` in test methods only; `logComment()` in helpers/pages
- No hardcoded credentials, URLs, or IDs — use properties files and Builders
- Do not assign return values you don't use

### Code quality
- No `System.out.println` in committed code
- No commented-out code
- No unused imports
- No unnecessary intermediate variables

---

## Patterns to Never Use

| Wrong | Correct |
|-------|---------|
| `Thread.sleep(2000)` | `WaitHelper.waitForElementToBeVisible(...)` |
| `locator.click()` | `click(locator, "name")` or `Element.click(config, locator, "name")` |
| `Assert.assertEquals(...)` | `AssertHelper.assertEquals(config, ...)` |
| `new Config()` in a test | receive from `dataProvider` |
| `config.getRunTimeProperty("environment")` | `Config.environment` |
| `WaitHelper.waitForElementToBeVisible(...)` in constructor | `assertPageLoaded(locator)` — inherited from `BasePage`, hard-fails if page does not load |
| `waitUntilLoaded()` / `@Override protected void waitUntilLoaded()` | remove both — call `assertPageLoaded(locator)` directly in the constructor |
| `config.logStep()` in a helper | `config.logComment()` |
| `config.logPass()` at end of test method | remove it — framework logs automatically |
| Hardcoded URL in test/page | put in properties file |
| Hardcoded credential in test | use CSV or `config.getRunTimeProperty()` |

---

## Reference Implementations (live, working code)

| What | File |
|------|------|
| API test class | [GitHubApiTest.java](src/test/java/automation/github/GitHubApiTest.java) |
| Web test class | [SauceDemoWebTest.java](src/test/java/automation/saucedemo/SauceDemoWebTest.java) |
| External API helper | [GitHubHelper.java](src/main/java/automation/modules/github/GitHubHelper.java) |
| API enum | [GitHubApi.java](src/main/java/automation/modules/github/api/GitHubApi.java) |
| Data POJO | [GitHubData.java](src/main/java/automation/modules/github/GitHubData.java) |
| Web page object | [LoginPage.java](src/main/java/automation/modules/github/web/LoginPage.java) |
| Framework base class | [TestBase.java](src/main/java/automation/core/TestBase.java) |
| Config system | [Config.java](src/main/java/automation/core/Config.java) |
| All enums | [Enums.java](src/main/java/automation/core/Enums.java) |
| ApiHelper (base for all helpers) | [ApiHelper.java](src/main/java/automation/core/api/ApiHelper.java) |
| AssertHelper | [AssertHelper.java](src/main/java/automation/core/AssertHelper.java) |
| JSON result report | `{resultsDirectory}/report.json` — read this after every run |
