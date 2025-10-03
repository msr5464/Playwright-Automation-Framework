<img src="https://raw.githubusercontent.com/msr5464/Basic-Automation-Framework/refs/heads/master/ThanosLogo.png" title="Powered by Thanos and created by Mukesh Rajput" height="50">

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
- **Video Recording** - Automatic video recording with configurable retention modes
- **Advanced Wait Strategies** - Smart wait utilities with Playwright auto-wait integration
- **Test Listener Integration** - Custom TestNG listeners for enhanced reporting
- **Session Management** - Login session storage and reuse capabilities

## 🛠️ Technology Stack
1. **Apache Maven 3.11.0** - Build automation and dependency management
2. **Java 21** - Stable Java version with wide compatibility
3. **Microsoft Playwright 1.54.0** - Modern browser automation with auto-wait capabilities
4. **TestNG Framework 7.9.0** - Advanced testing framework with parallel execution
5. **OpenCSV 5.7.1** - CSV file handling for test data
6. **Apache Commons Lang3 3.12.0** - Utility functions
7. **Chromium, Firefox & WebKit** - Cross-browser testing support

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
11. **Video Recording** - Automatic video recording with smart retention policies
12. **Advanced Wait Strategies** - Intelligent wait utilities with Playwright integration
13. **Test Listener Integration** - Custom TestNG listeners for enhanced reporting
14. **Session Management** - Login session storage and reuse for faster test execution

## Demo Test Cases
- **Web UI Testing** - Application login flow automation
- **Multi-Browser Testing** - Simultaneous browser testing
- **OTP Handling** - Two-factor authentication flow
- **Page Object Model** - Complete POM implementation
- **Session Management** - Login session storage and reuse
- **Video Recording** - Test execution video capture
- **Advanced Waits** - Smart wait strategies implementation

## 🎬 Advanced Features

### Video Recording
- **Automatic Recording** - Videos recorded for all test executions
- **Smart Retention** - Configurable retention policies (keep all, keep on failure, off)
- **Full HD Quality** - 1920x1080 video resolution
- **HTML Integration** - Video links embedded in test reports
- **Storage Optimization** - Automatic cleanup of unnecessary videos

### Wait Strategies
- **Playwright Auto-wait** - Leverages built-in auto-wait capabilities
- **Custom Wait Helpers** - Additional wait utilities for complex scenarios
- **Element Visibility** - Smart element visibility checks
- **Page Load Waits** - DOM content loaded waits
- **Optional Element Handling** - Graceful handling of optional UI elements

### Test Listeners
- **Custom TestNG Listeners** - Enhanced test reporting and execution tracking
- **Execution Time Tracking** - Detailed test execution time measurement
- **Test Status Monitoring** - Comprehensive test result tracking
- **Enhanced Logging** - Detailed test execution logs

### Session Management
- **Login Session Storage** - Automatic storage of successful login sessions
- **Session Reuse** - Skip login for subsequent tests using stored sessions
- **Cross-Test Persistence** - Sessions maintained across test executions
- **Performance Optimization** - Faster test execution by avoiding repeated logins

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
│       ├── HomePage.java     # Application home page
│       ├── LoginPage.java    # Login page
│       ├── OtpPage.java      # OTP verification page
│       └── DashboardPage.java # Dashboard page
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
- **Video Recordings**: `test-output/videos/` (based on VideoMode configuration)
- **Session Storage**: `src/test/resources/loginStorage/` (for session reuse)
- **Console Output**: Detailed logs in IDE console
- **Playwright Trace**: Built-in trace viewer for debugging

### Report Structure
```
test-output/
├── videos/                    # Video recordings (if enabled)
│   ├── testMethod1.webm      # Failed test videos
│   └── testMethod2.webm      # (based on VideoMode)
├── screenshots/               # Screenshots on failures
│   └── testMethod_timestamp.png
└── surefire-reports/         # TestNG reports
    └── index.html            # Main test report
```

## 🔧 Configuration
Update `src/main/resources/config.properties` for:
- **Browser selection** (chromium, firefox, webkit)
- **Environment settings** (test, staging, production)
- **Timeout configurations** (ObjectWaitTime)
- **Debug mode settings** (DebugMode)
- **Video recording** (VideoMode: off, on, retain-on-failure)
- **Viewport settings** (ViewportWidth, ViewportHeight)
- **Browser options** (Headless, SlowMo)
- **Execution settings** (EndExecutionOnFailure, RemoteExecution)

### Configuration Options
```properties
# Browser Configuration
Browser=chromium
Headless=false
SlowMo=0

# Environment Configuration
Environment=test
DebugMode=false

# Timeout Configuration
ObjectWaitTime=25

# Video Recording Configuration
VideoMode=retain-on-failure

# Viewport Configuration
ViewportWidth=1920
ViewportHeight=1080

# Execution Configuration
EndExecutionOnFailure=true
RemoteExecution=false
```

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
- **Video Recording**: Configure appropriate VideoMode for your testing needs
- **Session Management**: Use stored sessions for faster test execution
- **Wait Strategies**: Use WaitHelper for complex wait scenarios
- **Element Interactions**: Use Element helper for consistent element interactions
- **Configuration Management**: Use config.properties for environment-specific settings

## Creator
For any further help or queries, contact [Mukesh Rajput](https://www.linkedin.com/in/mukesh-rajput "LinkedIn Profile")