<img src="https://raw.githubusercontent.com/msr5464/Basic-Automation-Framework/refs/heads/master/Logo-full.png" title="Powered by Thanos and created by Mukesh Rajput" height="50">

# Jarvis — Test Automation Framework

An all-in-one test automation framework built on **Java 21 + Maven + TestNG**, covering **Web UI**, **REST API**, and **Mobile** testing with a shared core layer.

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Build | Maven | 3 |
| Test Runner | TestNG | 7.9.0 |
| Web Automation | Playwright | 1.54.0 |
| API Testing | REST-Assured | 5.3.2 |
| Mobile Automation | Appium Java Client | 9.3.0 |
| Data Generation | DataFaker | 2.5.3 |
| Database | MySQL Connector | 8.3.0 |
| Reporting | ReportPortal / ReportNG | 5.1.4 / 1.1.4 |
| Logging | Log4j2 | 2.22.1 |

---

## What This Framework Covers

```
┌─────────────────────────────────────────────────────────┐
│                  Jarvis Framework                        │
│                                                         │
│   ┌───────────┐   ┌───────────┐   ┌───────────────┐   │
│   │  Web UI   │   │   API     │   │    Mobile     │   │
│   │Playwright │   │REST-Assured│   │    Appium     │   │
│   └───────────┘   └───────────┘   └───────────────┘   │
│                                                         │
│         Shared Core: Config · Log · Assertions          │
│       UserPool · TokenCache · Database · TestRail       │
└─────────────────────────────────────────────────────────┘
```

| Testing Type | Technology | Platforms |
|---|---|---|
| **Web UI** | Playwright | Chromium, Firefox, WebKit |
| **REST API** | REST-Assured | Any HTTP/HTTPS endpoint |
| **Mobile** | Appium | Android, iOS — Local / BrowserStack |

---

## Project Structure

```
Jarvis2/
├── parameters/                        # Configuration files
│   ├── config.properties              # Base config (browser, env, timeouts)
│   ├── system.properties              # Secrets / local overrides (git-ignored)
│   ├── staging-sg.properties          # Per-environment + country overrides
│   └── ai-eval.properties             # AI evaluation scoring weights
│
├── src/main/java/automation/
│   ├── core/                          # Framework internals (shared across all test types)
│   │   ├── Config.java                # Central config — static globals + per-test state
│   │   ├── TestBase.java              # TestNG base class with lifecycle + user allocation
│   │   ├── BasePage.java              # Page Object base — element interactions + waits
│   │   ├── BrowserHelper.java         # Playwright init, screenshots, video, session storage
│   │   ├── Element.java               # Low-level Playwright element operations
│   │   ├── WaitHelper.java            # Explicit waits and loading helpers
│   │   ├── AssertHelper.java          # Soft assertions with logging
│   │   ├── Log.java                   # Colored HTML logging + TestNG Reporter
│   │   ├── DataGenerator.java         # Random data generation (names, emails, numbers)
│   │   ├── TestDataReader.java        # CSV reader with dynamic substitution
│   │   ├── DatabaseHelper.java        # MySQL query execution with retry + timing
│   │   ├── UserManagement.java        # Database-backed test user pool
│   │   ├── TokenManagement.java       # Thread-safe token cache with expiry
│   │   ├── TestContext.java           # Per-test state (users, data, etc.)
│   │   ├── TestVariables.java         # @TestVariables annotation (testrail, author)
│   │   ├── TestListener.java          # TestNG listener — soft assert + screenshots
│   │   ├── TestRailHelper.java        # TestRail result upload
│   │   ├── KnownBugTracker.java       # Skip/flag tests with known bugs
│   │   ├── EncryptionHelper.java      # Credential encryption/decryption
│   │   ├── SlackHelper.java           # Slack notification sender
│   │   ├── EmailHelper.java           # Email read via IMAP (OTP flows)
│   │   ├── PdfHelper.java             # PDF text extraction
│   │   ├── CmdHelper.java             # Shell command execution
│   │   ├── GenerateTestngXmlAndRun.java # Dynamic TestNG XML + CLI test runner
│   │   ├── Enums.java                 # Framework-wide enums
│   │   ├── api/
│   │   │   ├── BaseApiClient.java     # REST-Assured base with shared auth
│   │   │   ├── ApiHelper.java         # High-level API helper
│   │   │   └── ApiDetails.java        # API endpoint + method builder
│   │   └── mobile/
│   │       ├── AppiumDriverManager.java    # Android/iOS driver (local, farm, BrowserStack)
│   │       ├── BrowserStackHelper.java     # BrowserStack capabilities + session status
│   │       └── StartStopAppiumServer.java  # Local Appium server lifecycle
│   │
│   ├── modules/                       # Feature-specific helpers and pages
│   │   ├── access/
│   │   │   ├── AuthHelper.java
│   │   │   └── web/  LoginPage.java, DashboardPage.java
│   │   ├── cards/
│   │   │   ├── CardHelper.java, CardData.java, CardBuilder.java
│   │   │   ├── api/  CardApi.java
│   │   │   └── web/  CardListPage.java, CardPage.java
│   │   ├── payments/
│   │   │   ├── TransferData.java, TransferDataBuilder.java
│   │   │   └── RecipientData.java, RecipientDataBuilder.java
│   │   └── github/
│   │       ├── GitHubHelper.java
│   │       └── web/  HomePage.java, LoginPage.java, OtpPage.java, DashboardPage.java
│   │
│   └── aiEval/
│       └── AIEvalHelper.java          # Multi-dimensional AI response scoring
│
├── src/test/java/automation/
│   ├── cards/
│   │   ├── CardApiTest.java
│   │   └── CardWebTest.java
│   └── github/
│       └── GitHubLoginTest.java
│
└── src/test/resources/
    ├── CsvFiles/                      # Test data CSVs per feature
    └── loginStorage/                  # Stored browser sessions (git-ignored)
```

---

## Prerequisites

- **Java 21** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **Playwright browsers** — auto-downloaded on first run
- **Appium** (for mobile tests only) — `npm install -g appium`
- **Node.js** (for mobile tests only)

---

## Configuration

### Property Loading Order
Properties are loaded and merged in this order (later values override earlier):

1. `parameters/config.properties` — base defaults
2. `parameters/{environment}/config.properties` — env-subdirectory config
3. `parameters/{environment}-{country}.properties` — env + country overrides
4. `parameters/system.properties` — local developer overrides (**never commit real values**)

### Key Properties (`config.properties`)

| Key | Default | Description |
|---|---|---|
| `environment` | `staging` | Target environment |
| `browser` | `chromium` | Browser: `chromium`, `firefox`, `webkit`, `api` (API-only) |
| `headless` | `true` | Run browser headless |
| `country` | `sg` | Country code |
| `appLanguage` | `en` | App language |
| `ObjectWaitTime` | `30` | Element wait timeout (seconds) |
| `VideoMode` | `on_failure` | Video: `on`, `on_failure`, `off` |
| `debugMode` | `false` | Enable verbose logging |
| `githubUrl` | `https://github.com/` | GitHub base URL |
| `db.thanos.url` | — | MySQL JDBC connection string |

### Secrets (`system.properties` — git-ignored)

```properties
github.username=
github.password=
github.otp=
slackWebhookUrl=
testrailUrl=
testrailUser=
testrailPassword=
```

---

## Running Tests

### Option 1 — TestNG XML directly

```bash
mvn test
```

Uses `testng.xml` in the project root.

### Option 2 — Dynamic CLI runner

```bash
mvn exec:java -Dexec.args="<projectName> <environment> <browser> <groups> <country> <language> <debugMode> <uploadToTestrail> <isBrowserStack>"
```

**Arguments** (all optional, positional):

| # | Argument | Default | Example |
|---|---|---|---|
| 0 | projectName | `CustomerFrontend` | `Cards` |
| 1 | environment | `qa-1` | `staging` |
| 2 | browser | `chromium` | `firefox` / `api` |
| 3 | groups | `regression` | `regression,smokeTest` |
| 4 | country | `SG` | `HK` |
| 5 | appLanguage | `EN` | `ID` |
| 6 | debugMode | `false` | `true` |
| 7 | uploadToTestrail | `false` | `true` |
| 8 | isBrowserStack | `false` | `true` |

**Examples:**

```bash
# Web UI tests
mvn exec:java -Dexec.args="Cards staging chromium webCases SG EN false false false"

# API tests (browser init skipped automatically)
mvn exec:java -Dexec.args="Cards staging api apiCases SG EN false false false"

# Android mobile tests on BrowserStack
mvn exec:java -Dexec.args="Cards staging chromium androidCases SG EN false false true"

# GitHub login tests
mvn exec:java -Dexec.args="Github staging chromium smokeTest SG EN false false false"
```

### Option 3 — System properties override

```bash
mvn test -Denvironment=staging -Dcountry=sg -Dbrowser=firefox -Dheadless=false
```

---

## Writing Tests

### Test Class Structure

```java
public class MyFeatureTest extends TestBase
{
    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_WEB})
    @TestVariables(testrailData = "1:C0001:WEB", automatedBy = QA.Mukesh, country = Country.SG)
    public void myWebTest(Config config)
    {
        User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);
        MyHelper helper = new MyHelper(config);
        DashboardPage dashboard = helper.doLogin(user);
        AssertHelper.assertTrue(config, dashboard.isLoaded(), "Dashboard should load");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(testrailData = "1:C0002:API", automatedBy = QA.Mukesh, country = Country.SG)
    public void myApiTest(Config config)
    {
        MyApi api = new MyApi(config);
        Response response = api.getItems();
        AssertHelper.assertEquals(config, response.statusCode(), 200, "Status should be 200");
    }
}
```

### Data Providers

| Provider | Returns | Use when |
|---|---|---|
| `getConfig` | `Config` | Standard single-user test |
| `getTwoConfigs` | `Config, Config` | Two-actor tests (e.g., admin + employee) |
| `getMultipleConfigs` | `Config[]` | Multi-user flows |

### Test Groups

| Constant | Group Name | Use for |
|---|---|---|
| `GROUP_REGRESSION` | `regression` | Full regression suite |
| `GROUP_WEB` | `webCases` | Web UI tests (Playwright) |
| `GROUP_API` | `apiCases` | API tests (REST-Assured) |
| `GROUP_ANDROID` | `androidCases` | Android mobile tests (Appium) |
| `GROUP_IOS` | `iosCases` | iOS mobile tests (Appium) |
| `GROUP_SMOKE` | `smokeTest` | Smoke checks |
| `GROUP_CRITICAL` | `criticalFlows` | P0 flows |
| `GROUP_PROD_SANITY` | `prodSanity` | Production sanity |

### Adding a New Module

1. Create `src/main/java/automation/modules/{module}/` with:
   - `{Module}Helper.java` — orchestration (login, navigation, actions)
   - `web/{Page}Page.java` — page objects extending `BasePage` (for web)
   - `api/{Module}Api.java` — API endpoint definitions (for API)
2. Create `src/test/java/automation/{module}/{Module}Test.java` extending `TestBase`
3. Register the test class in `testng.xml` or `GenerateTestngXmlAndRun.discoverTestClasses()`

---

## Web UI Testing — Page Object Model

All pages extend `BasePage` (Playwright-based) which provides:

```java
click(locator, "Button name")
fillText(locator, text, "Field name")
getText(locator, "Label")
isElementDisplayed(locator)
waitForLoadingComplete()
inputOTP(otp, container)
navigateTo(url)
scrollToElement(locator, "name")
```

Each page defines its own load condition via `waitUntilLoaded()`:

```java
public class MyPage extends BasePage
{
    private final Locator header;

    public MyPage(Config config)
    {
        super(config);
        header = page.locator("h1.page-title");
        waitUntilLoaded(); // always last in constructor
    }

    @Override
    protected void waitUntilLoaded()
    {
        WaitHelper.waitForElementToBeVisible(config, header, "Page header");
    }
}
```

### Page Object Model Structure

```
Test Class
    └── ModuleHelper  (orchestrates flows)
            └── Page Objects  (wrap individual pages)
                    └── BasePage  (element interactions via Playwright)
                            └── Element / WaitHelper  (low-level operations)
```

---

## API Testing

Extend `ApiHelper` for each feature's API client. Auth headers (`Authorization`, `x-business-uuid`, `x-account-uuid`) are shared automatically across all clients within the same test.

```java
public class CardsApi extends ApiHelper
{
    public CardsApi(Config config) { super(config); }

    public Response createCard(CardData card)
    {
        ApiDetails endpoint = CardApi.CREATE_CARD.withPath("/api/v1/cards");
        return post(endpoint, card);
    }

    public Response getCard(String cardId)
    {
        ApiDetails endpoint = CardApi.GET_CARD.withPath("/api/v1/cards/" + cardId);
        return get(endpoint);
    }
}
```

Set `browser=api` in config (or pass `api` as the browser arg) to skip browser initialisation entirely for API-only test runs.

---

## Mobile Testing

### Local Device

```java
// In your test — AppiumDriverManager handles driver init automatically
config.isAndroid = true;
AppiumDriverManager.mobileDriver(config); // starts local Appium server + creates driver
```

Capabilities are loaded from `parameters/android.properties` or `parameters/ios.properties`.

### BrowserStack Cloud

```bash
mvn exec:java \
  -Dexec.args="MyApp staging chromium androidCases SG EN false false true" \
  -DbrowserStackUserName=<user> \
  -DbrowserStackAccessKey=<key>
```

Device config is read from `parameters/mobileConfiguration.json`. The framework picks a random device from the configured list for each test run.

---

## User Pool Management

Tests allocate users from a shared MySQL pool to avoid conflicts in parallel runs:

```java
// Allocate (marked BUSY in DB, auto-released in @AfterMethod)
User user = allocateUser(config, UserType.Admin, Feature.CARD, Country.SG);

// Full query builder for complex criteria
User user = allocateUser(config, "admin", q -> q
    .withUserType(UserType.Admin)
    .withFeature(Feature.CARD)
    .withCountry(Country.SG)
    .withoutFeature(Feature.DBS_SG));
```

DB keys in `system.properties`: `db.thanos.url`, `db.thanos.username`, `db.thanos.password`

---

## Session Storage (Web)

Avoid repeated logins by storing/restoring browser state:

```java
// Store session after successful login
BrowserHelper.storeSession(config, "MyLoginStorage.json");

// Load stored session in a later test (no login needed)
BrowserHelper.initBrowserWithStoredSession(config, "MyLoginStorage.json");
BrowserHelper.navigateTo(config, url);
```

Session files are saved to `src/test/resources/loginStorage/` (git-ignored).

---

## Key Utilities

| Class | Purpose |
|---|---|
| `DataGenerator` | Random names, emails, numbers, UUIDs |
| `TestDataReader` | Read CSV test data with `{randomString:8}` substitution |
| `DatabaseHelper` | `executeSelectQuery`, `executeSelectQueryWithRetry`, `executeSelectQueryAndReturnAllRows` |
| `TokenManagement` | Cache + reuse tokens; auto-refresh after 15 min |
| `EncryptionHelper` | Encrypt/decrypt credential values |
| `KnownBugTracker` | Mark tests as skipped for known bugs |
| `CmdHelper` | Run shell commands and capture stdout |
| `PdfHelper` | Extract text from PDF files |
| `SlackHelper` | Post test result notifications to Slack |
| `EmailHelper` | Read emails via IMAP for OTP/verification flows |

---

## Reports

| Report | Location | When |
|---|---|---|
| TestNG HTML | `target/surefire-reports/` | Every run |
| Screenshots | `test-results/screenshots/` | On failure |
| Videos | `test-results/videos/` | Based on `VideoMode` |
| TestRail | TestRail project | When `uploadToTestrail=true` |
| ReportPortal | ReportPortal dashboard | When RP agent configured |

---

## .gitignore Highlights

```
target/                               # Maven build output
test-output/                          # TestNG raw output
parameters/system.properties          # Secrets — never commit
src/test/resources/loginStorage/      # Stored browser sessions
```
