<img src="https://raw.githubusercontent.com/msr5464/Basic-Automation-Framework/refs/heads/master/Logo-full.png" title="Powered by Thanos and created by Mukesh Rajput" height="50">

# Jarvis - Playwright Automation Framework

A hybrid automation framework combining **Page Object Model** and **Data Driven** design, built on **Java 21 + Maven + TestNG**. Covers **Web UI** (Playwright — Chromium, Firefox, WebKit), **REST API** (REST-Assured), and **Mobile** (Appium) testing through a shared core layer. Each test method receives a fresh, isolated `Config` instance so tests never share state.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Running tests](#running-tests)
- [Project structure](#project-structure)
- [Writing tests](#writing-tests)
- [Web UI testing](#web-ui-testing)
- [API testing](#api-testing)
- [Mobile testing](#mobile-testing)
- [Test data](#test-data)
- [Adding a new module](#adding-a-new-module)
- [Key utilities](#key-utilities)
- [Reports](#reports)
- [Contributing](#contributing)
- [License](#license)
- [Author](#author)

---

## Tech stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Build | Maven | 3.8+ |
| Test runner | TestNG | 7.9.0 |
| Web automation | Playwright | 1.54.0 |
| API testing | REST-Assured | 5.3.2 |
| Mobile automation | Appium Java Client | 9.3.0 |
| Data generation | DataFaker | 2.5.3 |
| Database | MySQL Connector | 8.3.0 |
| Reporting | ReportPortal / ReportNG | 5.1.4 / 1.1.4 |

---

## Prerequisites

| Tool | Version | How to check |
|---|---|---|
| Java | 21 | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Playwright browsers | auto | downloaded on first `mvn test` |
| Node.js + Appium | LTS + any | `node -v` and `appium -v` — mobile tests only |

---

## Quick start

```bash
# 1. Clone the repo
git clone <repo-url> && cd Playwright-Automation-Framework

# 2. Create your local secrets file (never commit this)
cp parameters/staging-sg.properties parameters/system.properties
# Edit system.properties and fill in your credentials

# 3. Run the sample tests
mvn test -Dtest=SauceDemoWebTest -Denvironment=staging -Dcountry=SG
```

Playwright browsers are downloaded automatically on first run. No manual setup needed for web tests.

---

## Configuration

### How properties load

Later entries override earlier ones:

```
config.properties
  → staging-sg.properties   (environment + country overrides)
    → system.properties     (your local secrets — highest priority, git-ignored)
      → -D flags            (CLI overrides — highest priority of all)
```

### Key properties

| Property | Default | Description |
|---|---|---|
| `environment` | `staging` | `staging`, `qa-1`, `demo` |
| `browser` | `chromium` | `chromium`, `firefox`, `webkit`, `api` |
| `headless` | `false` | Run browser without UI |
| `country` | `sg` | `sg`, `hk`, `us`, `au`, `id`, `vn` |
| `ObjectWaitTime` | `30` | Element wait timeout in seconds |
| `VideoMode` | `on_failure` | `on`, `on_failure`, `off` |
| `debugMode` | `true` | Verbose step logging |
| `endExecutionOnFailure` | `false` | Stop suite on first failure |

### Local secrets (`parameters/system.properties`)

Create this file locally — it is git-ignored and will never be committed:

```properties
github.username=
github.password=
github.otp=
db.thanos.url=
db.thanos.username=
db.thanos.password=
slackWebhookUrl=
testrailUrl=
testrailUser=
testrailPassword=
```

---

## Running tests

### Run a single test method

```bash
mvn test -Dtest=SauceDemoWebTest#loginAndVerifyProductsPage -Denvironment=staging -Dcountry=SG
```

### Run an entire test class

```bash
mvn test -Dtest=SauceDemoWebTest -Denvironment=staging -Dcountry=SG
```

### Run by group

```bash
mvn test -DprojectName=SauceDemo -Denvironment=staging -Dbrowser=chromium -Dgroups=regression -Dcountry=SG
```

### Run via static `testng.xml`

```bash
mvn test
```

### Run API-only (no browser)

```bash
mvn test -Dtest=SauceDemoApiTest -Dbrowser=api -Denvironment=staging
```

### Run via dynamic CLI runner

```bash
mvn exec:java -Dexec.mainClass="automation.core.GenerateTestngXmlAndRun" \
  -Dexec.args="<projectName> <environment> <browser> <groups> <country> <language> <debugMode> <uploadToTestrail> <isBrowserStack>"
```

**Examples:**

```bash
# SauceDemo web tests
mvn exec:java -Dexec.args="SauceDemo staging chromium webCases SG EN false false false"

# GitHub API tests
mvn exec:java -Dexec.args="Github staging api apiCases SG EN false false false"

# Android on BrowserStack
mvn exec:java -Dexec.args="SauceDemo staging chromium androidCases SG EN false false true"
```

**CLI argument reference:**

| # | Argument | Default | Options |
|---|---|---|---|
| 0 | projectName | `CustomerFrontend` | `SauceDemo`, `Github` |
| 1 | environment | `qa-1` | `staging`, `qa-1`, `demo` |
| 2 | browser | `chromium` | `chromium`, `firefox`, `webkit`, `api` |
| 3 | groups | `regression` | `regression`, `smokeTest`, `apiCases`, `webCases` |
| 4 | country | `SG` | `SG`, `HK`, `US`, `AU`, `ID`, `VN` |
| 5 | appLanguage | `EN` | `EN`, `ID` |
| 6 | debugMode | `false` | `true`, `false` |
| 7 | uploadToTestrail | `false` | `true`, `false` |
| 8 | isBrowserStack | `false` | `true`, `false` |

---

## Project structure

```
Jarvis/
│
├── parameters/
│   ├── config.properties              # Base defaults (browser, env, timeouts)
│   ├── staging-sg.properties          # Environment + country overrides
│   ├── system.properties              # Your local secrets — git-ignored, never commit
│   └── knownBugsData.csv              # Known bug registry for auto-skip logic
│
├── src/main/java/automation/
│   │
│   ├── core/                          # Framework internals — do not modify per feature
│   │   ├── Config.java                # Central config — runtime properties + static globals
│   │   ├── TestBase.java              # TestNG base class — lifecycle, data providers, user allocation
│   │   ├── BasePage.java              # Page object base — Playwright wrappers for all UI interactions
│   │   ├── Element.java               # Low-level element ops used inside helpers (not page objects)
│   │   ├── WaitHelper.java            # All explicit wait strategies
│   │   ├── AssertHelper.java          # Soft assertions with automatic pass/fail logging
│   │   ├── Log.java                   # Coloured HTML logging + TestNG Reporter integration
│   │   ├── BrowserHelper.java         # Browser init, screenshots, video recording, session storage
│   │   ├── DataGenerator.java         # Random data — names, emails, numbers, UUIDs, dates
│   │   ├── TestDataReader.java        # CSV/Excel reader with dynamic placeholder support
│   │   ├── DatabaseHelper.java        # MySQL query execution with retry and timing
│   │   ├── UserManagement.java        # DB-backed test user pool (allocate/release)
│   │   ├── TokenManagement.java       # Thread-safe auth token cache with auto-expiry
│   │   ├── TestContext.java           # Per-test state container
│   │   ├── TestVariables.java         # @TestVariables annotation — author, country, TestRail
│   │   ├── TestListener.java          # TestNG listener — soft assert flush + failure screenshots
│   │   ├── JsonTestReporter.java      # Writes test-results/report.json after each run
│   │   ├── KnownBugTracker.java       # Auto-skip tests matched against knownBugsData.csv
│   │   ├── TestRailHelper.java        # TestRail result upload
│   │   ├── EncryptionHelper.java      # Credential encryption/decryption
│   │   ├── SlackHelper.java           # Slack run summary notifications
│   │   ├── EmailHelper.java           # IMAP email reader for OTP/verification flows
│   │   ├── PdfHelper.java             # PDF text extraction
│   │   ├── CmdHelper.java             # Shell command execution
│   │   ├── GenerateTestngXmlAndRun.java # Dynamic TestNG XML builder + CLI runner
│   │   ├── Enums.java                 # Framework-wide enums (UserType, Feature, Country, QA…)
│   │   ├── api/
│   │   │   ├── ApiDetails.java        # Interface for endpoint definitions
│   │   │   ├── BaseApiClient.java     # REST-Assured base with shared auth headers
│   │   │   ├── ApiHelper.java         # High-level execute* methods — extend this per module
│   │   │   └── PathBuilder.java       # Fluent path param builder for parameterised endpoints
│   │   └── mobile/
│   │       ├── AppiumDriverManager.java    # Android/iOS driver setup — local + BrowserStack
│   │       ├── BrowserStackHelper.java     # BrowserStack capabilities + session status reporting
│   │       └── StartStopAppiumServer.java  # Local Appium server lifecycle
│   │
│   └── modules/                       # One folder per product/feature area
│       ├── github/
│       │   ├── GitHubData.java        # POJO
│       │   ├── GitHubBuilder.java     # Fluent builder
│       │   ├── GitHubHelper.java      # Orchestration helper (extends ApiHelper)
│       │   ├── api/  GitHubApi.java   # Endpoint definitions
│       │   └── web/  HomePage  LoginPage  OtpPage  DashboardPage
│       │
│       └── saucedemo/
│           ├── PostData.java          # POJO for JSONPlaceholder post
│           ├── PostBuilder.java       # Fluent builder
│           ├── SauceDemoHelper.java   # Orchestration helper (extends ApiHelper)
│           ├── api/  PostApi.java     # JSONPlaceholder endpoint definitions
│           └── web/  LoginPage  ProductsPage  CartPage
│
├── src/test/java/automation/
│   ├── github/
│   │   ├── GitHubApiTest.java
│   │   └── GitHubLoginTest.java
│   └── saucedemo/
│       ├── SauceDemoApiTest.java
│       └── SauceDemoWebTest.java
│
└── src/test/resources/
    ├── github/csvFiles/
    │   └── github-users.csv           # GitHub test accounts per role + environment
    ├── saucedemo/csvFiles/
    │   ├── saucedemo-testdata.csv     # SauceDemo credentials per scenario + environment
    │   └── saucedemo-posts.csv        # Post data with dynamic placeholders
    └── loginStorage/                  # Stored browser sessions — git-ignored
```

---

## Writing tests

### Minimal example

```java
package automation.saucedemo;

import automation.core.*;
import automation.core.Enums.*;
import automation.modules.saucedemo.SauceDemoHelper;
import automation.modules.saucedemo.web.ProductsPage;
import org.testng.annotations.Test;
import java.util.Map;

public class SauceDemoWebTest extends TestBase {

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB},
          description = "Login and verify products page loads")
    @TestVariables(automatedBy = QA.Mukesh, country = Country.SG)
    public void loginAndVerifyProductsPage(Config config) {
        SauceDemoHelper sauceDemo = new SauceDemoHelper(config);
        Map<String, String> credentials = sauceDemo.getCredentials("login");

        config.logStep("Login to SauceDemo and verify the products page loads");
        ProductsPage products = sauceDemo.doLogin(credentials);

        AssertHelper.assertEquals(config, products.getPageTitle(), "Products", "Page title should be Products");
        AssertHelper.assertTrue(config, products.getProductCount() > 0, "At least one product should be visible");
    }
}
```

### Rules every test must follow

| Rule | Reason |
|---|---|
| Extend `TestBase` | Provides lifecycle, user pool, data providers |
| Use `dataProvider = "getConfig"` | Injects a fresh `Config` per test |
| Annotate with `@TestVariables` | Required for reporting and TestRail |
| Use `AssertHelper` not `Assert` | Soft assertions — test continues after failure |
| `config.logStep()` in tests only | Use `config.logComment()` inside helpers and pages |
| Never call `config.logPass()` at end of test | Framework logs PASS/FAIL automatically |
| Never call `Thread.sleep()` | Always use `WaitHelper` |

### Data providers

| Provider | Use when |
|---|---|
| `getConfig` | Standard single-user test |
| `getTwoConfigs` | Two-actor flows (e.g. admin + employee) |
| `getMultipleConfigs` | Multi-user flows |

### Test groups

Always include `GROUP_REGRESSION` plus one type group:

| Constant | String | Use for |
|---|---|---|
| `GROUP_REGRESSION` | `regression` | All standard tests — always include |
| `GROUP_WEB` | `webCases` | Playwright browser tests |
| `GROUP_API` | `apiCases` | REST-Assured API tests |
| `GROUP_SMOKE` | `smokeTest` | Quick pre-release smoke check |
| `GROUP_CRITICAL` | `criticalFlows` | Business-critical P0 flows |
| `GROUP_PROD_SANITY` | `prodSanity` | Production sanity after deploy |
| `GROUP_ANDROID` | `androidCases` | Android Appium tests |
| `GROUP_IOS` | `iosCases` | iOS Appium tests |

---

## Web UI testing

### Layer diagram

```
Test class
  └── ModuleHelper         orchestrates flows across pages
        └── Page objects   one class per page — locators + actions
              └── BasePage Playwright wrappers — click, fill, getText…
```

### Page object example

```java
public class ProductsPage extends BasePage {

    private final Locator addToCartButton = page.locator("[data-cy='add-to-cart']");

    public ProductsPage(Config config) {
        super(config);
        waitUntilLoaded();                    // always last in constructor
    }

    @Override
    protected void waitUntilLoaded() {
        WaitHelper.waitForElementToBeVisible(config, addToCartButton, "Add to Cart button");
    }

    public CartPage addToCart(String productId) {
        click(page.locator("[data-cy='add-to-cart-" + productId + "']"), "Add to Cart — " + productId);
        return new CartPage(config);          // always return next page
    }
}
```

### Key rules

- **Locators:** use `[data-cy='...']` first. Fall back to `id` → `name` → `css`. Avoid XPath.
- **Interactions:** never call Playwright directly (`locator.click()`). Always use `BasePage` or `Element` wrappers.
- **Waits:** `waitUntilLoaded()` in constructors only. Use `WaitHelper.waitForElementToBeVisible()` everywhere else.
- **Navigation:** every method that goes to a new page must return that page object.

### `BasePage` method reference

| Action | Method |
|---|---|
| Click | `click(locator, "name")` |
| Fill text | `fillText(locator, text, "name")` |
| Type char-by-char (autocomplete) | `typeText(locator, text, "name")` |
| Get text | `getText(locator, "name")` |
| Get input value | `getInputValue(locator, "name")` |
| Check checkbox | `check(locator, "name")` |
| Select dropdown | `selectOption(locator, value, "name")` |
| Is visible | `isElementDisplayed(locator)` |
| Scroll into view | `scrollToElement(locator, "name")` |
| JS click (overlap fallback) | `clickViaJS(locator, "name")` |

### `WaitHelper` reference

| Method | When to use |
|---|---|
| `waitForElementToBeVisible(config, locator, name)` | Standard — wait for element to appear |
| `waitForElementToBeHidden(config, locator, name)` | Wait for loader/spinner to disappear |
| `waitForOptionalElementToBeVisible(config, locator, name)` | Conditional element — 5 s timeout, returns boolean |
| `waitForElementToBeAttached(config, locator, name)` | Element in DOM but not yet visible |
| `waitForElementToBeDetached(config, locator, name)` | Wait for element removal |
| `waitForPageLoad(config)` | After navigation — page constructors only |
| `waitForNetworkIdle(config)` | After form submit with no visible confirmation |

### Session storage (skip repeated logins)

```java
// Save session after first login
BrowserHelper.storeSession(config, "GitHubLoginStorage.json");

// Restore in later tests — no login needed
BrowserHelper.initBrowserWithStoredSession(config, "GitHubLoginStorage.json");
BrowserHelper.navigateTo(config, url);
```

Session files are saved to `src/test/resources/loginStorage/` (git-ignored).

---

## API testing

### Endpoint enum pattern

```java
public enum PostApi implements ApiDetails {
    GetPosts  ("GET",    "/posts",        200),
    CreatePost("POST",   "/posts",        201),
    GetPost   ("GET",    "/posts/{id}",   200),
    DeletePost("DELETE", "/posts/{id}",   200);

    // standard boilerplate — see PostApi.java for full implementation
}
```

### Calling APIs

```java
SauceDemoHelper api = new SauceDemoHelper(config);

// Deserialised response
PostData created = api.execute(PostApi.CreatePost, new PostBuilder().withTitle("Hello").build(), PostData.class);

// Raw response — for negative / edge-case tests
Response response = api.executeRaw(PostApi.CreatePost, invalidPayload);
AssertHelper.assertEquals(config, response.getStatusCode(), 400, "Should reject invalid payload");

// Parameterised path
PostData post = api.execute(PostApi.GetPost.withPath("id", "1"), PostData.class);
```

Set `browser=api` to skip browser initialisation entirely for API-only runs.

---

## Mobile testing

### Local device

Add capabilities to `parameters/android.properties` or `parameters/ios.properties`, then:

```java
config.isAndroid = true;
AppiumDriverManager.mobileDriver(config);   // starts local Appium server + creates driver
```

### BrowserStack cloud

```bash
mvn exec:java \
  -Dexec.args="SauceDemo staging chromium androidCases SG EN false false true" \
  -DbrowserStackUserName=<user> \
  -DbrowserStackAccessKey=<key>
```

Device list is read from `parameters/mobileConfiguration.json` — a random device is picked per run.

---

## Test data

### Three-tier data strategy

| Data type | Where it lives | Example |
|---|---|---|
| Fully dynamic values | Builder in test code | `new PostBuilder().withTitle(DataGenerator.randomString(8))` |
| Reusable scenario data | CSV file | login credentials, cart scenarios |
| Environment-specific credentials | CSV with `environment` column | staging vs qa-1 accounts |
| Sensitive secrets | `system.properties` only — never CSV | real passwords, API keys |

### CSV lookup

```java
// Environment-aware — automatically picks the row matching Config.environment
Map<String, String> credentials = sauceDemo.getCredentials("login");

// Without environment filter (for data that's the same across all envs)
Map<String, String> row = TestDataReader.loadCsvRowByColumnValue(
    "saucedemo", "saucedemo-testdata", "scenario", "login");
```

### CSV file structure

Add an `environment` column whenever credentials or URLs differ per environment:

```
# saucedemo-testdata.csv
scenario,    environment, username,              password
login,       staging,     standard_user,         secret_sauce
login,       qa-1,        standard_user_qa,      secret_sauce
```

Omit the `environment` column for data that is identical across all environments (e.g. product names, expected page titles).

### Dynamic placeholders in CSV

Placeholders are resolved automatically at read time — no code change needed:

| Placeholder | Output |
|---|---|
| `{randomString:8}` | 8-char alphanumeric string |
| `{randomAlpha:5}` | 5-char alphabetic string |
| `{randomEmail}` | random email address |
| `{randomNumber:4}` | 4-digit number |
| `{randomUUID}` | UUID |
| `{currentDate}` | today `yyyy-MM-dd` |
| `{currentDate:dd/MM/yyyy}` | today in a custom format |
| `{dateOffset:7}` | 7 days from today |
| `{dateOffset:-1}` | yesterday |

```
# saucedemo-posts.csv
scenario,      title,             body
create_post,   {randomString:8},  {randomString:20}
```

### `DataGenerator` in code

```java
DataGenerator.randomString(8)           // 8-char alphanumeric
DataGenerator.randomAlphaString(5)      // 5-char alpha only
DataGenerator.randomEmail()             // random email
DataGenerator.randomNumber(10, 999)     // number in range
DataGenerator.randomUUID()              // UUID
DataGenerator.getDateWithOffset(7, "yyyy-MM-dd")  // 7 days from today
```

---

## Adding a new module

Follow this exact structure. Use `saucedemo` as the reference.

```
src/main/java/automation/modules/{feature}/
├── {Feature}Data.java        POJO — @JsonProperty + Lombok @Data
├── {Feature}Builder.java     fluent builder with .with*() methods
├── {Feature}Helper.java      orchestration — extends ApiHelper (external) or AuthHelper (internal app)
├── api/
│   └── {Feature}Api.java     enum implementing ApiDetails
└── web/
    └── {Page}Page.java       page object extending BasePage

src/test/java/automation/{feature}/
└── {Feature}Test.java        test class extending TestBase

src/test/resources/{feature}/csvFiles/
└── {feature}-testdata.csv    scenario + environment + test data columns
```

---

## Key utilities

| Class | What it does |
|---|---|
| `DataGenerator` | Random names, emails, numbers, UUIDs, dates |
| `TestDataReader` | CSV/Excel reader with placeholder substitution and env-aware lookup |
| `DatabaseHelper` | `executeSelectQuery`, `executeSelectQueryWithRetry`, `executeSelectQueryAndReturnAllRows` |
| `UserManagement` | Allocate/release test users from the DB pool |
| `TokenManagement` | Cache and reuse auth tokens — auto-refreshes after 15 min |
| `EncryptionHelper` | Encrypt/decrypt credential values stored in config |
| `KnownBugTracker` | Auto-skip tests that match a known bug pattern |
| `CmdHelper` | Run shell commands and capture stdout |
| `PdfHelper` | Extract text from PDF files |
| `SlackHelper` | Post run summary to Slack |
| `EmailHelper` | Read emails via IMAP for OTP and verification flows |

---

## Reports

After every run, check:

| Report | Location | When generated |
|---|---|---|
| JSON results | `test-results/report.json` | Every run |
| Screenshots | `test-results/screenshots/` | On test failure |
| Videos | `test-results/videos/` | Based on `VideoMode` setting |
| TestNG HTML | `target/surefire-reports/` | Every run |
| TestRail | TestRail project | When `uploadToTestrail=true` |
| ReportPortal | ReportPortal dashboard | When RP agent is configured |

---

## `.gitignore` highlights

```
target/                               # Maven build output
test-output/                          # TestNG raw output
parameters/system.properties          # Local secrets — never commit
src/test/resources/loginStorage/      # Stored browser sessions
```

---

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository and create a branch from `main`
2. **Branch naming** — use `feature/short-description` or `fix/short-description`
3. **Code style** — follow the patterns in `CLAUDE.md` (page objects, helpers, test structure)
4. **Tests** — include at least one test covering your change
5. **Pull request** — describe what you changed and why; link any related issues

For bugs or feature requests, please [open an issue](../../issues) with a clear description and reproduction steps.

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Author

**Mukesh Rajput**
- LinkedIn: [linkedin.com/in/mukesh-rajput](https://www.linkedin.com/in/mukesh-rajput)
- GitHub: [github.com/msr5464](https://github.com/msr5464)
