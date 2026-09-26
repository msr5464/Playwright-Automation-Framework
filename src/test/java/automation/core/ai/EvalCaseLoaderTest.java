package automation.core.ai;

import automation.core.AssertHelper;
import automation.core.Config;
import automation.core.Enums.*;
import automation.core.TestBase;
import automation.core.TestVariables;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class EvalCaseLoaderTest extends TestBase
{
    private static final String CASE_ONE = "{\"caseId\": \"RAG-001\", \"suite\": \"smoke\", \"tags\": [\"rag\"], "
        + "\"input\": \"How long is the return window?\", \"mustContain\": [\"30 days\"]}";
    private static final String CASE_TWO = "{\"caseId\": \"RAG-002\", \"suite\": \"smoke\", \"input\": \"Where is my order?\"}";

    private final EvalCaseLoader loader = new EvalCaseLoader();

    private File tempDirectory() throws IOException
    {
        File directory = Files.createTempDirectory("eval-cases").toFile();
        directory.deleteOnExit();
        return directory;
    }

    private File writeFile(File directory, String name, String json) throws IOException
    {
        File file = new File(directory, name);
        file.deleteOnExit();
        Files.writeString(file.toPath(), json);
        return file;
    }

    private String loadError(String json) throws IOException
    {
        try
        {
            loader.loadFromFile(writeFile(tempDirectory(), "case.json", json));
            return "";
        }
        catch (IllegalArgumentException e)
        {
            return e.getMessage();
        }
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loadFromFile_singleCase(Config config) throws IOException
    {
        List<EvalCase> cases = loader.loadFromFile(writeFile(tempDirectory(), "case.json", CASE_ONE));

        AssertHelper.assertEquals(config, cases.size(), 1, "Number of cases loaded");
        AssertHelper.assertEquals(config, cases.get(0).getCaseId(), "RAG-001", "Case ID");
        AssertHelper.assertEquals(config, cases.get(0).getMustContain().get(0), "30 days", "Required phrase");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loadFromFile_arrayOfCases(Config config) throws IOException
    {
        List<EvalCase> cases = loader.loadFromFile(
            writeFile(tempDirectory(), "cases.json", "[" + CASE_ONE + "," + CASE_TWO + "]"));

        AssertHelper.assertEquals(config, cases.size(), 2, "Number of cases loaded from an array");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loadFromFile_missingCaseId_rejected(Config config) throws IOException
    {
        AssertHelper.assertContains(config, loadError("{\"input\": \"question\"}"), "caseId",
            "Loader rejects a case without caseId");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loadFromFile_missingInput_rejected(Config config) throws IOException
    {
        AssertHelper.assertContains(config, loadError("{\"caseId\": \"RAG-003\"}"), "input",
            "Loader rejects a case without input");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void filterByTag_keepsOnlyTaggedCases(Config config)
    {
        List<EvalCase> cases = List.of(
            EvalCase.builder().caseId("RAG-001").tags(List.of("rag")).build(),
            EvalCase.builder().caseId("AGENT-001").tags(List.of("agent")).build(),
            EvalCase.builder().caseId("UNTAGGED-001").build());

        List<EvalCase> ragCases = loader.filterByTag(cases, "rag");

        AssertHelper.assertEquals(config, ragCases.size(), 1, "Number of rag-tagged cases");
        AssertHelper.assertEquals(config, ragCases.get(0).getCaseId(), "RAG-001", "Tagged case ID");
    }

    @Test(dataProvider = "getConfig", groups = {GROUP_REGRESSION, GROUP_API})
    @TestVariables(automatedBy = QA.Mukesh)
    public void loadSuite_readsJsonFilesInSubfolders(Config config) throws IOException
    {
        File baseDirectory = tempDirectory();
        File suiteDirectory = new File(baseDirectory, "smoke");
        File nestedDirectory = new File(suiteDirectory, "nested");
        nestedDirectory.mkdirs();
        suiteDirectory.deleteOnExit();
        nestedDirectory.deleteOnExit();
        writeFile(suiteDirectory, "case-one.json", CASE_ONE);
        writeFile(nestedDirectory, "case-two.json", CASE_TWO);
        writeFile(suiteDirectory, "notes.txt", "not a case");

        List<EvalCase> cases = loader.loadSuite(baseDirectory, "smoke");

        AssertHelper.assertEquals(config, cases.size(), 2, "Cases loaded from the suite folder and its subfolder");
    }
}
