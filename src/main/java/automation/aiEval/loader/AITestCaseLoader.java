package automation.aiEval.loader;

import automation.aiEval.model.AITestCase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AITestCaseLoader
{
    private final ObjectMapper mapper = new ObjectMapper();

    public List<AITestCase> loadFromFile(File file) throws IOException
    {
        List<AITestCase> cases = new ArrayList<>();

        String content = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();

        if (content.startsWith("["))
        {
            AITestCase[] array = mapper.readValue(file, AITestCase[].class);
            cases.addAll(Arrays.asList(array));
        }
        else
        {
            AITestCase testCase = mapper.readValue(file, AITestCase.class);
            cases.add(testCase);
        }

        for (AITestCase tc : cases)
        {
            if (tc.getCaseId() == null || tc.getCaseId().isBlank())
            {
                throw new IllegalArgumentException(
                    "AITestCase [" + file.getName() + "] missing required field: caseId");
            }
            if (tc.getInput() == null)
            {
                throw new IllegalArgumentException(
                    "AITestCase [" + file.getName() + "] missing required field: input");
            }
        }

        return cases;
    }

    public List<AITestCase> loadFromDirectory(File directory) throws IOException
    {
        List<AITestCase> all = new ArrayList<>();
        if (!directory.isDirectory())
        {
            return all;
        }

        File[] files = directory.listFiles();
        if (files == null)
        {
            return all;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                all.addAll(loadFromDirectory(file));
            }
            else if (file.getName().endsWith(".json"))
            {
                all.addAll(loadFromFile(file));
            }
        }

        return all;
    }

    public List<AITestCase> loadSuite(File baseDir, String suite) throws IOException
    {
        File suiteDir = new File(baseDir, suite);
        return loadFromDirectory(suiteDir);
    }

    public List<AITestCase> filterByTag(List<AITestCase> cases, String tag)
    {
        List<AITestCase> filtered = new ArrayList<>();
        for (AITestCase tc : cases)
        {
            if (tc.getTags() != null && tc.getTags().contains(tag))
            {
                filtered.add(tc);
            }
        }
        return filtered;
    }
}
