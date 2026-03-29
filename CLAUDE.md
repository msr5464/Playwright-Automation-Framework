# Jarvis / Thanos-pw — AI Agent Guide

This file is the primary orientation for AI agents working in this repo. Read it before writing any code.

---

## Framework Overview

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Build | Maven 3.8+ |
| Test Runner | TestNG 7.9.0 |
| Web UI | Playwright 1.54.0 |
| API | REST-Assured 5.3.2 |
| Mobile | Appium 9.3.0 |
| Reporting | ReportPortal + ReportNG |

Tests live in `src/test/java/automation/`. Framework internals live in `src/main/java/automation/core/`. Feature-specific helpers live in `src/main/java/automation/modules/`.

---

## Running Tests

**Run a single test method (use this to verify a fix without running the full suite):**
```bash
mvn test -Dtest=CardApiTest#createAndVerifyCard -Denvironment=staging -Dcountry=SG
```

**Run an entire test class:**
```bash
mvn test -Dtest=CardApiTest -Denvironment=staging -Dcountry=SG
```

**Run by group (preferred for full suite runs):**
```bash
mvn test -DprojectName=Cards -Denvironment=staging -Dbrowser=chromium -Dgroups=regression -Dcountry=SG
```

**Legacy positional args (still works, but avoid for new scripts):**
```bash
mvn exec:java -Dexec.mainClass="automation.core.GenerateTestngXmlAndRun" \
  -Dexec.args="Cards staging chromium regression SG EN false false false"
```

**Via static testng.xml:**
```bash
mvn test
```

**Key system properties:**

| Property | Values | Default |
|----------|--------|---------|
| `projectName` | Cards, Budget, Claims, Payment, Access | CustomerFrontend |
| `environment` | staging, qa-1, demo | staging |
| `browser` | chromium, firefox, webkit, api | chromium |
| `groups` | regression, smokeTest, apiCases, webCases, criticalFlows, prodSanity | regression |
| `country` | SG, HK, ID | SG |
| `headless` | true, false | true |

---

## Test Results

After a run, check:
- `test-results/report.json` — machine-readable JSON with pass/fail per test, failure message, screenshot path, duration
- `test-results/screenshots/` — failure screenshots (PNG)
- `test-results/videos/` — videos (based on VideoMode config)
- `target/surefire-reports/` — TestNG HTML reports

---

## How to Write a Test

### 1. Minimal test structure

```java
package automation.{feature};

import automation.core.*;
import automation.core.Enums.*;
import org.testng.annotations.Test;

public class MyFeatureTest extends TestBase {

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API},
          description = "Create a card and verify it is returned by the list endpoint")
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void createAndVerifyCard(Config config) {
        User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);
        CardHelper cards = new CardHelper(config);
        cards.loginAndSetAuth(user);

        CardData card = new CardBuilder().withCardName("Marketing").build();
        CardData created = cards.createCard(card);

        AssertHelper.assertNotNull(config, created.getId(), "Card ID should be set after creation");
        AssertHelper.assertEquals(config, created.getCardName(), card.getCardName(), "Card name matches");
    }
}
```

### 2. Key rules

- Always extend `TestBase`
- Always use `dataProvider = "getConfig"` (or `"getTwoConfigs"` for two-actor tests)
- Always call `allocateUser(config, UserType, Feature, Country)` — this reserves a user from the DB pool and auto-releases it after the test
- Always pass `config` to every helper/page constructor
- Annotate every `@Test` method with `@TestVariables`
- Use `AssertHelper` for all assertions (not `Assert` directly) — it logs pass/fail with context

### 3. Test groups (use constants from TestBase)

| Constant | String | Use |
|----------|--------|-----|
| `GROUP_REGRESSION` | `"regression"` | All standard tests |
| `GROUP_API` | `"apiCases"` | API-only tests |
| `GROUP_WEB` | `"webCases"` | Browser UI tests |
| `GROUP_SMOKE` | `"smokeTest"` | Smoke subset |
| `GROUP_CRITICAL` | `"criticalFlows"` | Business-critical |
| `GROUP_PROD_SANITY` | `"prodSanity"` | Production smoke |

Always include at least `GROUP_REGRESSION` plus one of `GROUP_API` or `GROUP_WEB`.

---

## How to Add a New Feature Module

Follow this exact structure (use `cards` as the reference implementation):

```
src/main/java/automation/modules/{feature}/
├── {Feature}Data.java          # POJO with @JsonProperty + Lombok @Data
├── {Feature}Builder.java       # Fluent builder with .with*() methods + build()
├── {Feature}Helper.java        # High-level orchestration (extends AuthHelper)
├── api/
│   └── {Feature}Api.java       # Enum implementing ApiDetails — endpoint definitions
└── web/
    └── {Page}Page.java         # Page objects (extend BasePage)

src/test/java/automation/{feature}/
└── {Feature}Test.java          # Test class (extends TestBase)

src/test/resources/{feature}/csvFiles/
└── {feature}-testcases.csv     # Only if data is reusable across multiple flows (see Test Data section)
```

### Data POJO pattern
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class WidgetData {
    @JsonProperty("widget_name") private String widgetName;
    @JsonProperty("widget_type") private String widgetType;
    // response-only fields (set by API, not sent in request):
    @JsonProperty("id")          private String id;
    @JsonProperty("status")      private String status;
}
```

### Builder pattern
```java
public class WidgetBuilder {
    private String widgetName;
    private String widgetType = "Standard";   // default

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

### API enum pattern
```java
public enum WidgetApi implements ApiDetails {
    CreateWidget("POST",   "/v1/widgets",      201),
    GetWidget(   "GET",    "/v1/widgets/{id}", 200),
    DeleteWidget("DELETE", "/v1/widgets/{id}", 200);

    // standard boilerplate — copy from CardApi.java
}
```

### Page object pattern
```java
public class WidgetListPage extends BasePage {
    private final Locator createButton = page.locator("[data-cy='create-widget-btn']");

    public WidgetListPage(Config config) {
        super(config);
        waitUntilLoaded();
    }

    @Override protected void waitUntilLoaded() {
        WaitHelper.waitForElementToBeVisible(config, createButton, "Create Widget button");
    }

    public WidgetPage clickCreate() {
        click(createButton, "Create Widget button");
        return new WidgetPage(config);
    }
}
```

**All UI locators use `data-cy` attributes.** Never use CSS class names, XPath, or IDs unless `data-cy` is unavailable.

---

## Configuration System

Config loads in this order (later entries override earlier):

1. `parameters/config.properties` — base defaults
2. `parameters/{environment}.properties` — env-specific overrides
3. `parameters/{environment}-{country}.properties` — env + country overrides
4. `parameters/system.properties` — local developer secrets (git-ignored, never commit)
5. `-D` system properties — CLI overrides (highest priority)

Access config values in tests via:
```java
config.getRunTimeProperty("github.username")   // runtime property
Config.environment                              // static global
Config.browser                                 // static global
```

---

## User Allocation

Users are stored in the database with a `usageStatus` of BUSY or FREE. Always allocate via `allocateUser()` — never hardcode credentials.

```java
// Single user
User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

// Two users (for multi-actor tests)
Config[] configs = getTwoConfigs();   // use as dataProvider = "getTwoConfigs"
User admin    = allocateUser(configs[0], UserType.Admin,    Feature.CARD, Country.SG);
User employee = allocateUser(configs[1], UserType.Employee, Feature.CARD, Country.SG);
```

Users are **automatically released** after each test in `@AfterMethod`. Do not release manually.

---

## API Tests

```java
CardHelper cards = new CardHelper(config);
cards.loginAndSetAuth(user);                         // sets auth headers for all API calls

CardData created  = cards.createCard(card);          // POST → deserializes response
CardData fetched  = cards.getCard(created.getId());  // GET  → deserializes response
cards.deleteCard(created.getId());                   // DELETE → asserts 200
```

For raw response access (negative tests):
```java
Response response = api.executeRaw(CardApi.CreateCard, invalidPayload);
AssertHelper.assertEquals(config, response.statusCode(), 400, "Should reject invalid payload");
```

---

## Assertions

Always use `AssertHelper` — it logs a colored PASS/FAIL entry and ties to the soft-assert context.

```java
AssertHelper.assertEquals(config, actual, expected, "Descriptive message");
AssertHelper.assertTrue(config, condition, "Descriptive message");
AssertHelper.assertNotNull(config, value, "Descriptive message");
AssertHelper.assertContains(config, text, substring, "Descriptive message");

// Element assertions (Playwright)
AssertHelper.assertElementVisible(config, locator, "Element name");
AssertHelper.assertElementText(config, locator, "Expected text", "Element name");
```

---

## Logging

Use `config.log*()` instance methods — shorter since `config` is always in scope:

```java
config.logStep("Login as Admin user and navigate to Cards list page");    // test classes only — orange
config.logComment("Clicking create card button");                          // helpers and page objects — grey
config.logPass("Card created and verified successfully");                  // green
config.logFail("Card not found in list");                                  // red + screenshot
config.logWarning("Optional element not present, continuing");             // yellow
```

The static `Log.*` methods (`Log.step(config, "...")`, `Log.comment(config, "...")`) are equivalent and can be used interchangeably, but `config.log*()` is preferred for brevity.

**Scope rule:** `config.logStep()` is for **test classes only**. Use `config.logComment()` inside helpers and page objects.

---

## Element Interactions

Never call Playwright methods directly (`locator.click()`, `locator.fill()`, `locator.textContent()`). Always use the framework wrappers which add logging, waiting, and retry logic.

**In page objects** (extend `BasePage`) — use inherited methods, no `config` arg needed:

| Action | Method |
|--------|--------|
| Click | `click(locator, "Button name")` |
| Fill / type | `fillText(locator, text, "Field name")` |
| Type char-by-char (e.g. autocomplete) | `typeText(locator, text, "Field name")` |
| Get text | `getText(locator, "Label name")` |
| Get input value | `getInputValue(locator, "Field name")` |
| Check checkbox | `check(locator, "Checkbox name")` |
| Select dropdown | `selectOption(locator, value, "Dropdown name")` |
| Hover | `hover(locator, "Element name")` |
| Scroll to | `scrollToElement(locator, "Element name")` |
| Is visible | `isElementDisplayed(locator)` |
| JS click (fallback for overlapping elements) | `clickViaJS(locator, "Button name")` |

**In helpers** (do not extend `BasePage`) — use `Element` static methods:

| Action | Method |
|--------|--------|
| Click | `Element.click(config, locator, "Button name")` |
| Fill | `Element.enterData(config, locator, text, "Field name")` |
| Get text | `Element.getText(config, locator, "Label name")` |
| Is visible | `Element.isElementDisplayed(config, locator, "name")` |

---

## WaitHelper

Never use `Thread.sleep()`. Always use `WaitHelper`:

| Method | When to use |
|--------|-------------|
| `WaitHelper.waitForElementToBeVisible(config, locator, name)` | Standard — wait for element to appear in action methods |
| `WaitHelper.waitForElementToBeHidden(config, locator, name)` | Wait for loaders, spinners, toasts to disappear |
| `WaitHelper.waitForOptionalElementToBeVisible(config, locator, name)` | Conditional elements — 5s timeout, returns boolean |
| `WaitHelper.waitForElementToBeAttached(config, locator, name)` | Element is in DOM but may not be visible yet |
| `WaitHelper.waitForElementToBeDetached(config, locator, name)` | Wait for element to be removed from DOM |
| `WaitHelper.waitForAnyElementToBeDisplayed(config, locator...)` | First of several possible elements to appear |
| `WaitHelper.waitForPageLoad(config)` | After page navigation — **only inside page object constructors** |
| `WaitHelper.waitForNetworkIdle(config)` | After form submissions with no visible confirmation |
| `WaitHelper.waitForLoadingComplete(config, spinnerLocator)` | Wait for a specific loading spinner locator to disappear |

**Critical distinction — mirrors CONVENTIONS.md rule:**
- `waitUntilLoaded()` (calls `waitForPageLoad` internally) → **only in page constructors**
- `WaitHelper.waitForElementToBeVisible()` → **everywhere else** (action methods, verifications)

---

## Test Data

### Primary pattern — use Builder directly in the test

Most test data is fully dynamic and created inline using Builders. This is the standard pattern:

```java
CardData card = new CardBuilder()
    .withCardName("Marketing Card")
    .withSpendingLimit("5000")
    .withRandomColor()
    .build();
```

Builder defaults are already set for optional fields (`cardPurpose`, `sourceOfFunds`, `cardType`), so only specify what is relevant to the test scenario.

Use `DataGenerator` when you need random values:
```java
DataGenerator.randomAlphaString(8)   // random 8-char string
DataGenerator.randomEmail()           // random email
DataGenerator.randomFullName()        // random full name
DataGenerator.randomNumber(10, 100)   // random number in range
```

### When to use a CSV file

Only create a CSV when the data is **reusable across multiple different flows** and mixes static and dynamic values. Do NOT create a CSV just for a single test case.

| Situation | Approach |
|-----------|----------|
| All values are dynamic / unique per run | Builder in the test class |
| Data is test-specific | Variable passed through Builder |
| Data is reused across multiple flows | CSV file |

CSV files live in `src/test/resources/{feature}/csvFiles/`. Read them via:
```java
List<Map<String, String>> rows = TestDataReader.readCsv("feature/csvFiles/data.csv");
String value = rows.get(0).get("columnName");
```

Supported CSV placeholders:

| Placeholder | Output |
|-------------|--------|
| `{randomString:8}` | 8-char random alphanumeric |
| `{randomEmail}` | Random email address |
| `{randomNumber:4}` | 4-digit random number |

---

## Coding Guidelines (from Thanos project standards)

These rules apply when writing any code in this repo. They are enforced during code review.

### Page Object classes (`web/{Page}Page.java`)

- Each page class owns **only** the locators and actions for that one page. Never put locators from another page inside a different page class.
- Locator preference: `data-cy` attribute > `id` > `name` > `css` > `xpath`. Avoid XPath unless nothing else works.
- When XPath is unavoidable, use `contains()` — never exact text match:
  ```java
  // Good
  page.locator("//button[contains(text(),'Submit')]")
  // Bad — breaks on whitespace or minor text changes
  page.locator("//button[text()='Submit']")
  ```
- Never use positional XPath (`//div[1]/span[2]`), deep nesting (`//body/main/section/div/article/button`), or auto-generated hash class names (`.v-btn--abc123`).
- Every method that navigates away must **return the next page object** to maintain chaining:
  ```java
  public CardPage clickCreateCard() {
      click(createCardButton, "Create Card button");
      return new CardPage(config);    // ← always return the next page
  }
  ```
- Call `waitUntilLoaded()` from the constructor only. Elsewhere use `WaitHelper.waitForElementToBeVisible()`.
- Use Thanos/framework methods (`Element.click`, `BasePage.click`, etc.) — never call Playwright APIs directly on the locator.

### Helper classes (`{Feature}Helper.java`)

- Create a method in a Helper only when it **orchestrates two or more page objects**. If you are chaining calls on one page only, add that method to the page class itself.
- Static / shared test data belongs in a dedicated `StaticData` helper, not scattered in test classes.
- Do not instantiate page objects in test classes — use instances and factory methods provided by the Helper.

### Test classes (`{Feature}Test.java`)

- Every test must use its **own dedicated user** — no two test methods share the same user account or business account.
- Use `Log.step()` inside test methods only. Use `Log.comment()` inside helpers and page objects.
- `config.logStep()` messages must be written in plain English describing the full action and expected outcome so anyone can follow the test flow by reading the steps alone:
  ```java
  // Good
  config.logStep("Login as Admin user and navigate to Cards list page");
  config.logStep("Create a virtual card with spending limit 1000 SGD and verify it appears in the card list");

  // Bad
  config.logStep("createCard");
  ```
- Do not pass data directly as hardcoded literals in test methods. Use Builder methods or CSV test data.
- If you do not use the return value of a method, do not assign it to a variable:
  ```java
  cards.deleteCard(id);         // ← correct, return value not needed
  CardData d = cards.deleteCard(id);  // ← wrong if d is never used
  ```

### Naming conventions

- Variable names must be **full descriptive names** — no abbreviations:
  - `merchantName` not `merchName`
  - `orderId` not `orderID`
  - `spendingLimit` not `limit`
- Method names must reflect the action performed:
  - `fillDetailsAndSubmit()`, `navigateToCardList()`, `verifyCardIsVisible()`
- Enum values use **CamelCase** (not ALL_CAPS):
  - `SpringGreen`, `BerryBlue` — correct
  - `SPRING_GREEN` — wrong

### CSV test data rules

- Create a CSV file when the data is **reusable across multiple flows** or when data mixes static and dynamic values.
- If all values are dynamic (generated at runtime), hardcode them via Builder in the test class instead.
- If data is specific to a single test case only, pass it as a variable from the test — do not create a CSV just for it.
- All URLs must be in properties files, not hardcoded in tests or page objects.

### Code quality

- Remove all debug output (`System.out.println`, temporary log lines) before committing.
- Do not push commented-out code or unused imports.
- Do not create unnecessary intermediate variables:
  ```java
  // Good
  AssertHelper.assertEquals(config, testData.get("cardName"), expected, "Card name matches");

  // Bad — extra variable serves no purpose
  String name = testData.get("cardName");
  AssertHelper.assertEquals(config, name, expected, "Card name matches");
  ```
- Prefer `assertElementText` over `assertElementDisplayed` wherever you also need to verify text content.
- Merge methods that perform the same type of action by parameterizing them — don't duplicate logic with slight variations.

---

## Known Patterns to Avoid

- Do NOT use `Thread.sleep()` — always use `WaitHelper` methods
- Do NOT call Playwright locator methods directly (`locator.click()`, `locator.fill()`) — use `BasePage` or `Element` wrappers
- Do NOT use `Assert.*` directly — always use `AssertHelper.*`
- Do NOT hardcode credentials — always use `allocateUser()` + user pool
- Do NOT use XPath or CSS class-based locators — use `[data-cy='...']`
- Do NOT create a new `Config` manually — always receive it from `dataProvider`
- Do NOT release users manually — `@AfterMethod` handles it
- Do NOT call `config.getRunTimeProperty()` for environment/browser/country — use the static fields on `Config`
- Do NOT instantiate page objects directly in test classes — use the Helper
- Do NOT use `Log.step()` inside helpers or page objects — only in test methods
- Do NOT call `waitUntilLoaded()` outside a page constructor — use `WaitHelper.waitForElementToBeVisible()` elsewhere
- Do NOT share users or accounts between test methods
- Do NOT hardcode URLs — put them in properties files

---

## Reference Implementations

| What | Where |
|------|-------|
| API test (CRUD) | [CardApiTest.java](src/test/java/automation/cards/CardApiTest.java) |
| Web UI test | [CardWebTest.java](src/test/java/automation/cards/CardWebTest.java) |
| Data POJO | [CardData.java](src/main/java/automation/modules/cards/CardData.java) |
| Builder | [CardBuilder.java](src/main/java/automation/modules/cards/CardBuilder.java) |
| Helper | [CardHelper.java](src/main/java/automation/modules/cards/CardHelper.java) |
| API enum | [CardApi.java](src/main/java/automation/modules/cards/api/CardApi.java) |
| Page object | [CardListPage.java](src/main/java/automation/modules/cards/web/CardListPage.java) |
| Framework base | [TestBase.java](src/main/java/automation/core/TestBase.java) |
| Config system | [Config.java](src/main/java/automation/core/Config.java) |
| All enums | [Enums.java](src/main/java/automation/core/Enums.java) |
