package automation.core.ai;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvalCaseLoader
{
    private final ObjectMapper mapper = new ObjectMapper();

    public List<EvalCase> loadFromFile(File file) throws IOException
    {
        List<EvalCase> cases = new ArrayList<>();
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();

        if (content.startsWith("["))
        {
            EvalCase[] array = mapper.readValue(file, EvalCase[].class);
            cases.addAll(Arrays.asList(array));
        }
        else
        {
            cases.add(mapper.readValue(file, EvalCase.class));
        }

        for (EvalCase evalCase : cases)
        {
            if (evalCase.getCaseId() == null || evalCase.getCaseId().isBlank())
            {
                throw new IllegalArgumentException(
                    "EvalCase [" + file.getName() + "] missing required field: caseId");
            }
            if (evalCase.getInput() == null || evalCase.getInput().isBlank())
            {
                throw new IllegalArgumentException(
                    "EvalCase [" + file.getName() + "] missing required field: input");
            }
        }

        return cases;
    }

    public List<EvalCase> loadFromDirectory(File directory) throws IOException
    {
        List<EvalCase> all = new ArrayList<>();
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

    public List<EvalCase> loadSuite(File baseDir, String suite) throws IOException
    {
        return loadFromDirectory(new File(baseDir, suite));
    }

    public List<EvalCase> filterByTag(List<EvalCase> cases, String tag)
    {
        List<EvalCase> filtered = new ArrayList<>();
        for (EvalCase evalCase : cases)
        {
            if (evalCase.getTags() != null && evalCase.getTags().contains(tag))
            {
                filtered.add(evalCase);
            }
        }
        return filtered;
    }
}
