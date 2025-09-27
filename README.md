# Playwright Automation Framework
This is a modern Hybrid Automation Framework created using properties of `Page Object Model` and `Data Driven` automation frameworks.
The framework is built using **Microsoft Playwright** with Java 21, TestNG and Maven for Web-based automation, providing fast, reliable, and cross-browser testing capabilities.

## 🌟 Key Features
- **Playwright 1.54.0** - Latest browser automation features and improved performance
- **TestNG 7.9.0** - Enhanced test reporting and parallel execution
- **Java 21** - Stable and widely supported Java version
- **Complete Test Isolation** - Each test method gets fresh Config instances
- **Multi-Browser Support** - Support for Chromium, Firefox, and WebKit
- **Automatic Test Name Extraction** - Test case names automatically populated from TestNG
- **Enhanced Screenshot Capabilities** - Built-in screenshot functionality
- **CSV Data Integration** - Test data management with CSV files
- **Modern API Design** - Clean, maintainable code structure

## 🛠️ Technology Stack
1. **Apache Maven 3.11.0** - Build automation and dependency management
2. **Java 21** - Stable Java version with wide compatibility
3. **Microsoft Playwright 1.54.0** - Modern browser automation with auto-wait capabilities
4. **TestNG Framework 7.9.0** - Advanced testing framework with parallel execution
5. **OpenCSV 5.7.1** - CSV file handling for test data
6. **Apache Commons Lang3 3.12.0** - Utility functions
7. **Chromium, Firefox & WebKit** - Cross-browser testing support

## 🌟 Why Playwright?

### Performance Benefits
- **Auto-wait**: No more explicit waits - Playwright automatically waits for elements
- **Faster Execution**: Up to 3x faster than traditional WebDriver approaches
- **Parallel Execution**: Built-in support for parallel test execution
- **Network Interception**: Built-in network request/response handling

### Reliability Benefits
- **Auto-retry**: Automatic retry of failed actions
- **Better Error Messages**: Clear, actionable error messages
- **Cross-browser Consistency**: Same API across all browsers
- **Mobile Testing**: Built-in mobile device emulation

### Developer Experience
- **Modern API**: Intuitive and easy-to-use API
- **Rich Debugging**: Built-in debugging tools and trace viewer
- **Type Safety**: Better IDE support and type checking
- **Active Development**: Regular updates and new features

# What is Test Automation Framework?
A "Test Automation Framework" is scaffolding that is laid to provide an execution environment for the automation test scripts. The framework provides the user with various benefits that help them to develop, execute and report the automation test scripts efficiently. It is more like a system that has created specifically to automate our tests.

In a very simple language, we can say that a framework is a constructive blend of various guidelines, coding standards, concepts, processes, practices, project hierarchies, modularity, reporting mechanism, test data injections etc. to pillar automation testing. Thus, the user can follow these guidelines while automating application to take advantages of various productive results.

The advantages can be in different forms like the ease of scripting, scalability, modularity, understandability, process definition, re-usability, cost, maintenance etc. Thus, to be able to grab these benefits, developers are advised to use one or more of the Test Automation Framework.

# What is the Page Object Model?
The Page Object Model is a design pattern of testing, derived from the Object Oriented Programming concepts. The POM describes the web application into the number of web pages being used and contains the elements as properties and actions as methods. This offers you low maintenance on the tests developed.

![Alt text](https://solutionscafe.files.wordpress.com/2014/01/untitled10.png "Page Object Model Example")

# 🌟 MAIN FEATURES OF THIS PROJECT

## Core Framework Features
1. **Configuration Management** - Flexible config.properties file system
2. **Data-Driven Testing** - CSV file integration for dynamic test data
3. **Screenshot on Failure** - Automatic screenshot capture for debugging
4. **Soft Assertions** - Complete test execution with detailed reporting
5. **CSV Integration** - Test data management with CSV files
6. **Cross-Browser Support** - Chromium, Firefox, and WebKit browsers
7. **Comprehensive Logging** - Detailed test execution logs and reports
8. **Complete Test Isolation** - Each test method gets fresh Config instances
9. **Multi-Browser Support** - Support for multiple browser instances simultaneously
10. **Automatic Test Naming** - Test case names automatically extracted from TestNG

## Playwright-Specific Features
- **Auto-wait Capabilities** - No need for explicit waits
- **Built-in Screenshots** - Native screenshot functionality
- **Network Interception** - Built-in request/response handling
- **Mobile Emulation** - Device emulation capabilities
- **Trace Viewer** - Built-in debugging and trace analysis
- **Parallel Execution** - Native parallel test execution support

## Demo Test Cases
- **Web UI Testing** - GitHub login flow automation
- **Multi-Browser Testing** - Simultaneous browser testing
- **OTP Handling** - Two-factor authentication flow
- **Page Object Model** - Complete POM implementation

# 📁 CODE STRUCTURE

## Package Organization
```
src/
├── main/java/
│   ├── helpers/              # Framework utility classes
│   │   ├── BaseTest.java     # Base test class with TestNG integration
│   │   ├── BrowserHelper.java # Playwright browser management
│   │   ├── Element.java      # Web element interactions and actions
│   │   ├── WaitHelper.java   # Wait utilities (Playwright auto-wait)
│   │   ├── AssertHelper.java # Assertion utilities
│   │   ├── TestDataReader.java # CSV data reading
│   │   ├── DataGenerator.java # Test data generation
│   │   ├── Log.java          # Logging utilities
│   │   └── Config.java       # Configuration management
│   └── pageObjects/          # Page Object Model classes
│       ├── HomePage.java     # GitHub home page
│       ├── LoginPage.java    # GitHub login page
│       ├── OtpPage.java      # OTP verification page
│       └── DashboardPage.java # GitHub dashboard page
└── test/java/
    └── TestLoginFlows.java   # Test case implementations
```

## Key Components
1. **Helpers Package** - Core framework utilities and reusable methods
2. **PageObjects Package** - Page Object Model implementation for web pages
3. **Tests Package** - Test case implementations using TestNG
4. **Resources Package** - Configuration files and test data
5. **CSV Files** - Test data management with CSV files

# 🚀 EXECUTION STEPS

## Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- Playwright browsers will be installed automatically

## Setup Instructions
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Playwright
   ```

2. **Import project in your IDE**
   - **IntelliJ IDEA**: Open as Maven project
   - **Eclipse**: Import as "Existing Maven Project"
   - **VS Code**: Open folder and install Java extension pack

3. **Install TestNG plugin** (if using Eclipse)

4. **Install Playwright browsers** (automatic on first run)
   ```bash
   mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
   ```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=TestLoginFlows
```

### Run with Specific Browser
```bash
mvn test -Dbrowser=chromium
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit
```

## 📊 OUTPUT & REPORTS
- **TestNG Reports**: `target/surefire-reports/index.html`
- **Screenshots**: `test-output/` (automatic on failures)
- **Console Output**: Detailed logs in IDE console
- **Playwright Trace**: Built-in trace viewer for debugging

## 🔧 Configuration
Update `src/main/resources/config.properties` for:
- Browser selection (chromium, firefox, webkit)
- Environment settings
- Timeout configurations
- Debug mode settings

Update `src/main/resources/test.properties` for:
- Test-specific configurations
- Environment overrides

# 🚀 FURTHER IMPLEMENTATIONS

## Adding New Test Cases
1. **Create new test methods** in existing test classes or create new test classes
2. **Extend BaseTest** - All test classes should extend BaseTest for framework features
3. **Use Page Objects** - Leverage existing page objects or create new ones
4. **Add Helper Methods** - Extend existing helper classes as needed

## Best Practices
- **Extend BaseTest**: All test classes should extend BaseTest
- **Use Page Object Model**: Implement POM for maintainable tests
- **Leverage Auto-wait**: Playwright's built-in auto-wait capabilities
- **Data-Driven Testing**: Use CSV files for test data
- **Proper Assertions**: Use AssertHelper for consistent assertions
- **Screenshot on Failure**: Automatic screenshot capture is built-in

## Playwright-Specific Best Practices
- **Use Locators**: Prefer `page.locator()` over `page.querySelector()`
- **Leverage Auto-wait**: No need for explicit waits in most cases
- **Use Built-in Assertions**: Playwright's built-in assertion methods
- **Network Interception**: Use for API testing and mocking
- **Trace Viewer**: Use for debugging complex scenarios

# 🔧 Framework Architecture

## Test Isolation
- **Fresh Config per Test**: Each test method gets a new Config instance
- **Complete Isolation**: Tests don't interfere with each other
- **Automatic Cleanup**: Browser instances are automatically closed after each test

## Configuration Management
- **Environment-based**: Support for multiple environments (test, staging, prod)
- **Property Override**: Runtime property overrides via TestNG parameters
- **Flexible Settings**: Easy configuration updates without code changes

## Logging and Reporting
- **Comprehensive Logging**: Detailed test execution logs
- **Test Status Tracking**: Pass/Fail/Skip status with execution time
- **Screenshot Integration**: Automatic screenshots on failures
- **TestNG Integration**: Full TestNG reporting capabilities

# 📈 Performance Benefits

## Framework Optimizations
- **Efficient Browser Management**: Shared browser instances where possible
- **Smart Wait Strategies**: Leveraging Playwright's auto-wait
- **Optimized Selectors**: Using Playwright's powerful locator strategies
- **Minimal Setup Time**: Fast test initialization

## Playwright Advantages
- **Auto-wait**: Reduces flaky tests by automatically waiting for elements
- **Fast Execution**: Optimized browser automation engine
- **Cross-browser**: Consistent behavior across Chromium, Firefox, and WebKit
- **Modern API**: Intuitive and developer-friendly interface