package automation;

import org.testng.annotations.Test;
import automation.core.DataGenerator;
import automation.core.TestBase;
import automation.core.TestVariables;
import automation.core.Config;
import automation.core.GenerateTestngXmlAndRun;
import automation.core.Enums.QA;


public class TestDemo extends TestBase {
    @TestVariables(testrailData = "1:C0001:WEB", automatedBy = QA.Mukesh, maintainedBy = QA.Mukesh)
    @Test(dataProvider = "getConfig", description = "This test will help to execute tests locally, the same way Jenkins does")
    public void testJenkinsSetup(Config testConfig) {
        String projectName            = "fullsuite";
        String environment            = "staging";
        String browserName            = "chromium";
        String groupNames             = "regression";
        String country                = "SG";
        String appLanguage            = "EN";
        String branchName             = "main";
        String jobBuildTag            = DataGenerator.randomAlphaNumericString(10);
        String sendEmailTo            = "false";
        String sendSlackMessage       = "false";
        String debugMode              = "false";
        String uploadToTestrail       = "false";
        String updateAutomationStatus = "false";
        String bsExecution            = "false";
        String isRemoteExecution      = "true";

        GenerateTestngXmlAndRun.main(new String[] {
                projectName, environment, browserName, groupNames, country, appLanguage,
                branchName, jobBuildTag, sendEmailTo, sendSlackMessage, debugMode,
                uploadToTestrail, updateAutomationStatus, bsExecution, isRemoteExecution
        });
    }

    @TestVariables(testrailData = "1:C0001:WEB", automatedBy = QA.Mukesh)
    @Test(timeOut = DEFAULT_TEST_TIMEOUT, dataProvider = "getConfig", description = "This is demo testcase 1", groups = {
            "dummyGroup" })
    public void exampleTestcase1(Config testConfig) {
        testConfig.logPass("This testcases is PASSED intentionally to show the example fo successful case");
    }

    @TestVariables(testrailData = "1:C0002:API", automatedBy = QA.Mukesh)
    @Test(timeOut = DEFAULT_TEST_TIMEOUT, dataProvider = "getConfig", description = "This is demo testcase 2", groups = {
            "dummyGroup" })
    public void exampleTestcase2(Config testConfig) {
        testConfig.logFail("This testcases is FAILED intentionally to show the example fo failed case");
    }
}