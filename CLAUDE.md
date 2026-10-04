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

The API call is inline only to keep this skeleton short. Real tests put request building, API
chains and response parsing behind a Helper method — see *Coding Rules › Test classes*.

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

Follow this exact structure. Use `automation.modules.github` as the live reference for API
modules and `automation.modules.saucedemo` for web flows.

```
src/main/java/automation/modules/{feature}/
├── {Feature}Data.java          # POJO — @Data @NoArgsConstructor @AllArgsConstructor + @JsonProperty
├── {Feature}Builder.java       # Fluent builder — .with*() methods + withDefaults() + build()
├── {Feature}Helper.java        # Extends ApiHelper — the module's flow API: the business operations tests call
├── {Feature}Enums.java         # Option enums (one nested enum per choice the UI offers) — only when the module has one
├── api/
│   └── {Feature}Api.java       # Enum implementing ApiDetails — one entry per endpoint
└── web/
    └── {Page}Page.java         # Extends BasePage — locators + actions for ONE page only

src/test/java/automation/{feature}/
└── {Feature}Test.java          # Extends TestBase — one @Test method per scenario
```

### Exact imports for framework classes

`automation.core` is **flat**. A file living in `modules/{feature}/web/` does *not*
import from a matching `automation.core.web` — **there is no such package**, and no
`automation.modules.core` either. Copy these exactly:

| Class | Import |
|-------|--------|
| `BasePage` | `import automation.core.BasePage;` |
| `Config` | `import automation.core.Config;` |
| `TestBase` | `import automation.core.TestBase;` |
| `AssertHelper` | `import automation.core.AssertHelper;` |
| `WaitHelper` | `import automation.core.WaitHelper;` |
| `BrowserHelper` | `import automation.core.BrowserHelper;` |
| `Element` | `import automation.core.Element;` |
| `Log` | `import automation.core.Log;` |
| `TestVariables` | `import automation.core.TestVariables;` |
| `ApiHelper` | `import automation.core.api.ApiHelper;` — the one class NOT flat |
| `ApiDetails`, `PathBuilder` | `import automation.core.api.ApiDetails;` etc. |
| enums (`QA`, `Country`, …) | `import automation.core.Enums.*;` |

Only `automation.core.api` and `automation.core.mobile` are sub-packages. Everything
else listed under `core/` sits directly in `automation.core`.

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

    public WidgetHelper(Config config) {
        // Read the base URL from the properties file — never a literal, and never a
        // `static final` constant, which cannot reach config. Inline it in super(...):
        // an instance field cannot be referenced before the supertype constructor runs.
        super(config, config.getRunTimeProperty("widget.api.url"));
    }

    public WidgetHelper(Config config, String authToken) {
        this(config);
        if (authToken != null) setAuthToken(authToken);
    }
}
```

### Page object
```java
package automation.modules.widget.web;          // the file's OWN package

import com.microsoft.playwright.Locator;
import automation.core.BasePage;                // NOT automation.core.web.BasePage
import automation.core.Config;
import automation.core.Log;

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

### Option enums and business operations (web flows)

A module's Helper holds one public field per page of the module, and the page objects chain:
every action that leaves a page returns the next page object. The test stores each page it is
handed on the Helper's field for that page and calls the next action on it, so every step
continues from the page the previous step returned. The Helper's own operations open the
app (the one place a page object is constructed, right after navigating) and run a whole
sequence for tests that check nothing in between. A choice among options the UI offers is an
enum parameter, never part of a method name.

```java
// WidgetEnums.java — one class per module, a nested enum per choice. The values are every
// option the page offers; key = the attribute value the option's locator is keyed on.
public class WidgetEnums {
    public enum DeliverySpeed {
        Standard("standard", "Standard"),
        Express("express", "Express");

        private final String key;
        private final String label;

        DeliverySpeed(String key, String label) { this.key = key; this.label = label; }

        public String getKey()   { return key; }
        public String getLabel() { return label; }
    }
}
```

```java
// Page object: ONE selection method. Its locator is the verified selector with only the
// key replaced, so every option is selectable through the same code. An option that keeps
// the user on this page returns `this`; when the option decides which page comes next, the
// method returns BasePage, builds the page each automated option lands on, and the caller
// casts.
public BasePage chooseDelivery(DeliverySpeed speed) {
    click(page.locator("[data-option='" + speed.getKey() + "']"), speed.getLabel() + " delivery");
    return switch (speed) {
        case Standard -> new StandardDeliveryPage(config);
        default -> throw new UnsupportedOperationException(speed.getLabel() + " delivery is not automated yet");
    };
}

// A value a check compares is returned ready to compare, converted by the Helper's one
// converter. Never a private converter copied into each page.
public String getTotal() {
    return WidgetHelper.toPlainAmount(getText(totalDisplay, "Order total"));
}
```

```java
// Helper: one public field per page. The entry operation opens the app and returns the page
// it lands on; a composed operation continues from the pages already in the fields. Neither
// constructs a page mid-flow, and operations never assert.
public WidgetListPage widgetListPage;
public WidgetSummaryPage widgetSummaryPage;
public StandardDeliveryPage standardDeliveryPage;
public OrderPage orderPage;

public WidgetSummaryPage createWidget(WidgetData widget) {          // entry: navigate, then chain
    BrowserHelper.navigateTo(config, config.getRunTimeProperty("widget.url"));
    widgetListPage = new WidgetListPage(config);                     // the only page a Helper builds
    return widgetListPage.clickCreate().fill(widget).submit();
}

public OrderPage orderWidget(DeliverySpeed speed) {                 // composed: continues from widgetSummaryPage
    switch (speed) {
        case Standard -> {
            standardDeliveryPage = (StandardDeliveryPage) widgetSummaryPage.chooseDelivery(speed);
            orderPage = standardDeliveryPage.confirm();
        }
        default -> throw new UnsupportedOperationException(speed.getLabel() + " delivery is not automated yet");
    }
    return orderPage;
}

public static String toPlainAmount(String shown) { ... }            // the module's one converter
```

```java
// Test: each step continues from the page the previous step returned, stored on the Helper's
// field for that page; that step's checks read the page right after it.
config.logStep("Create the widget and verify the summary shows its name");
widgets.widgetSummaryPage = widgets.createWidget(widget);
AssertHelper.assertEquals(config, widgets.widgetSummaryPage.getWidgetName(), widget.getWidgetName(), "Summary should show the widget name");

config.logStep("Choose standard delivery, confirm, and verify the order shows the same widget");
widgets.standardDeliveryPage = (StandardDeliveryPage) widgets.widgetSummaryPage.chooseDelivery(DeliverySpeed.Standard);
widgets.orderPage = widgets.standardDeliveryPage.confirm();
AssertHelper.assertEquals(config, widgets.orderPage.getWidgetName(), widget.getWidgetName(), "Order should be for the created widget");
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

Which call you use is decided by **which kind of class you are in**, not by what you
want to say. There are exactly two rules:

| In a… | Use | Never |
|-------|-----|-------|
| **test class** (`src/test/java/...`, `extends TestBase`) | `config.logStep("...")` | `Log.step`, `Log.comment`, `config.logComment` |
| **any other class** (page objects, helpers, builders) | `Log.comment(config, "...")` | `config.logStep`, `Log.step` |

```java
// In a test class — one logStep per step of the scenario:
config.logStep("Login to GitHub and verify dashboard loads");

// In a page object or helper:
Log.comment(config, "Clicking create repository button");
```

`Log.step()` exists but is **not for module code** — a page object that calls it puts a
step line in the report for something that is not a step, and the run report stops being
a readable list of what the test did. `automation.modules.github.web.LoginPage` does this;
it is a known violation, not a pattern to copy. `automation.modules.saucedemo.web.LoginPage`
is the correct reference.

Also available in test classes:
```java
config.logPass("Repository created with correct name");          // intermediate confirmations only
config.logFail("Repository not found in list");                  // red + screenshot
config.logWarning("Optional banner not present, continuing");    // non-blocking issues
```

- One `config.logStep()` per business step, stating the action and its expected outcome,
  immediately before the calls that carry it out and followed by that step's checks —
  never one run-on logStep for the whole test, and never one per click.
- Do **NOT** add `config.logPass()` as the last line of a test — the framework logs
  PASS/FAIL automatically after each test

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
| `waitForPageToSettle(config)` | After an action that updates the page in place — typing that triggers a lookup, a selection that recalculates a total — before the next action or read. A click made while the page is still redrawing can be lost |
| `waitForNetworkIdle(config)` | After a form submission that navigates. Returns at once on a page that has already loaded, so it never waits for an in-place update |

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

### URLs are properties — add the key BEFORE you read it

**`getRunTimeProperty` returns `null` for a key that is not in the file.** It does not
throw and does not warn (the debug line only prints with `debugMode=true`). So a key
that was never added fails far from its cause — `BrowserHelper.navigateTo` now rejects
a null URL by name, but any other consumer just gets null.

Every URL a test or page navigates to needs a key in
`parameters/{environment}-{country}.properties` **before** any code reads it:

| What | Key | Example |
|------|-----|---------|
| Module base URL | `{module}.url` | `saucedemo.url=https://www.saucedemo.com/` |
| A specific page | `{module}.{page}.url` | `naukari.login.url=https://www.naukri.com/nlogin/login` |
| API base URL | `{module}.api.url` | `widget.api.url=https://api.widget.io` |

`{module}` is lowercase and matches the package name (`naukari`, `saucedemo`). The page
segment is the last meaningful path segment — `/nlogin/login` → `login`. Never name a key
after an id, hash or date; those identify one record, not one page.

(`githubUrl` predates this convention. Do not copy its shape for anything new.)

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

### Builder (primary pattern — build inside Helper methods, not in `@Test`)
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

- **Module-scoped, one file per business entity**: keep a module's data in its own `csvFiles/` folder and name each file for the entity it holds — `users.csv`, `products.csv`, `orders.csv` — not for the test layer. API and web tests that use the same entity read the same sheet.
- Add rows for a new scenario; never rewrite or drop rows that other tests read.
- Login secrets never go into a new CSV: a password, token or API key, or an OTP the login asks for. A CSV is committed and this repository is public. They belong in the properties file (see *Configuration System*), read with `config.getRunTimeProperty()`. Other values a flow types are test data and go in the CSV, including a payment card number, its CVV and a bank page's OTP. The existing credential sheets predate this rule.

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
- Helper operations are named for the business action and take the choice as a parameter:
  `makePayment(PaymentMethod.CreditCard, order)`, never `payByCreditCard()`
- Enum values in CamelCase: `SpringGreen`, `BerryBlue` — not `SPRING_GREEN`

### Page objects
- One class = one page. No cross-page locators.
- Several small actions on the same page in a row (filling a form's fields) become one higher-level method — `fillCheckoutDetails(data)` — so the test calls one action, not five.
- Locator priority: `[data-cy='...']` > `#id` > `[name='...']` > css > xpath
- Element inside an iframe: enter the frame in the field itself, one `.frameLocator(...)` per nested iframe — `page.frameLocator("#checkout-frame").frameLocator("iframe[title='3ds']").locator("#amount")`. It is still a `Locator`, so `click`, `fillText`, `getText` and `assertPageLoaded` work unchanged. Pick each iframe by a stable `#id`, `[title='...']` or `[name^='stem']` — never by position, or by a per-load name or URL token. A selector written `A >> internal:control=enter-frame >> B` (failure messages print them this way) is exactly this: `page.frameLocator("A").locator("B")`
- XPath: use `contains()` only — never exact text match, positional selectors, or deep nesting
- Page objects chain: an action that leaves the page returns the next page object; an action that stays returns `this`. When the option chosen decides the next page, the method returns `BasePage` and builds the page each automated option lands on — the caller casts, `(CardFormPage) paymentPage.choosePaymentMethod(PaymentMethod.Card)`
- A getter returns what the page shows. When a check compares it in another form (an amount as plain number text, a phone as its digits), the getter converts it through the module Helper's one `public static` converter — never a converter copied into each page
- Call `assertPageLoaded(locator)` at the end of every constructor — no `waitUntilLoaded()` override needed
- An option picked from a set (a payment method, a delivery speed) is selected by ONE method taking
  the module's option enum. Its locator is a verified selector with only the option's key replaced —
  the one locator built in a method rather than declared in the constructor. Fix its fixed parts;
  never write one option's value into it, which silently breaks every other option

### Helpers — the module's flow API
- **Reuse before anything new.** Search this module and `automation.core` for a method that already
  does it. Otherwise change one slightly so it serves both callers — a new enum value and its case, an
  overload that keeps the old signature, an optional data field — without changing what it does for
  its current callers. Only then write a new method. A method that differs from an existing one only
  by a hard-coded value is never new.
- **One public field per page of the module** (`public PaymentPage paymentPage;`). The test stores every
  page it is handed on the field for that page; the Helper's operations continue from those fields.
- Business operations a person would name (`checkout`, `makePayment`, `confirmOtp`), each covering as
  many pages as the operation takes and returning the page it lands on. An **entry** operation opens
  the app (navigate, log in, start a checkout) — the ONLY place a page object is constructed, right
  after navigating. A **composed** operation continues from the pages in the fields, storing each page
  it passes through, for tests that check nothing in between. Never `new XPage(config)` mid-flow: the
  page the previous step returned is already in its field.
- Choices are parameters: a fixed set of options is an enum in the module's `{Feature}Enums` class;
  options the module does not automate yet throw `UnsupportedOperationException`.
- Operations never assert — the test does, on the pages they return.
- The same sequence is never written twice — not in two tests, not in two operations.
- JSON extraction (`response.jsonPath().getList(...)`), Java Stream filtering/mapping, loops and multi-step data preparation live in the Helper, which returns what the test asserts on.
- No thin wrapper: a method that only renames one existing call and adds nothing.
- Do not instantiate page objects in test classes — use the Helper

### Test classes
- **Each step continues the chain**: a `@Test` method reads like the scenario — each step calls a Helper operation or a business-level page method on the page the previous step returned, stores the page it returns on the Helper's field (`shop.paymentPage = shop.cartPage.checkout(order);`), and is followed by that step's `AssertHelper` checks, read from that page. Never hold a page in a local variable, and never construct one in a test. No loops, Stream filtering or JSONPath extraction inside it.
- **Short**: about 25-30 lines between the method's braces. Longer usually means small page actions that belong in one page method, or a sequence that belongs in a Helper operation.
- **Hide API intricacies**: request bodies (`new XBuilder()...build()`) and chains of dependent API calls are built and run inside the Helper, not in the `@Test` method.
- **Test data is one setup line**: a Helper method reads the module's CSV and builds the Data object (`PaymentData payment = shop.buildPayment("card")`) — never a Builder chain, a CSV read or a literal in the test.
- One user per test — never share accounts between test methods
- `config.logStep()` in test classes only; `Log.comment(config, ...)` everywhere else
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
| `config.logStep()` or `Log.step()` in a helper or page object | `Log.comment(config, "...")` |
| `Log.comment(config, ...)` / `Log.step(...)` in a test class | `config.logStep("...")` |
| `config.logPass()` at end of test method | remove it — framework logs automatically |
| Hardcoded URL in test/page | put in properties file |
| Hardcoded credential in test | use `config.getRunTimeProperty()` (an existing credential sheet's CSV row where the module already reads one) |

---

## Reference Implementations (live, working code)

| What | File |
|------|------|
| API test class | [GitHubApiTest.java](src/test/java/automation/github/GitHubApiTest.java) |
| Web test class | [SauceDemoWebTest.java](src/test/java/automation/saucedemo/SauceDemoWebTest.java) |
| External API helper | [GitHubHelper.java](src/main/java/automation/modules/github/GitHubHelper.java) |
| Helper business operations (web) | [SauceDemoHelper.java](src/main/java/automation/modules/saucedemo/SauceDemoHelper.java) |
| API enum | [GitHubApi.java](src/main/java/automation/modules/github/api/GitHubApi.java) |
| Data POJO | [GitHubData.java](src/main/java/automation/modules/github/GitHubData.java) |
| Web page object | [ProductsPage.java](src/main/java/automation/modules/saucedemo/web/ProductsPage.java) |
| Framework base class | [TestBase.java](src/main/java/automation/core/TestBase.java) |
| Config system | [Config.java](src/main/java/automation/core/Config.java) |
| All enums | [Enums.java](src/main/java/automation/core/Enums.java) |
| ApiHelper (base for all helpers) | [ApiHelper.java](src/main/java/automation/core/api/ApiHelper.java) |
| AssertHelper | [AssertHelper.java](src/main/java/automation/core/AssertHelper.java) |
| JSON result report | `{resultsDirectory}/report.json` — read this after every run |
