package automation.core;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multi-source test data reader supporting Excel and CSV files.
 *
 * <h3>Dynamic placeholders in CSV values</h3>
 * Placeholders are resolved at read time — no code changes needed when data changes.
 *
 * <pre>
 * {randomString:8}       — 8-char random alphanumeric string
 * {randomAlpha:5}        — 5-char alphabetic-only string
 * {randomEmail}          — random email address
 * {randomNumber:4}       — 4-digit random number
 * {randomUUID}           — random UUID
 * {currentDate}          — today's date (yyyy-MM-dd)
 * {currentDate:dd/MM/yyyy} — today's date in a custom format
 * {dateOffset:7}         — date 7 days from today (yyyy-MM-dd)
 * {dateOffset:-1}        — yesterday (negative offset supported)
 * </pre>
 *
 * <h3>Environment-specific test data</h3>
 * Add an {@code environment} column to any CSV to provide different values per env:
 *
 * <pre>
 * scenario,  environment, username,      password
 * checkout,  staging,     buyer_staging, secret1
 * checkout,  qa-1,        buyer_qa1,     secret2
 * </pre>
 *
 * Then load with the environment-aware overload:
 * <pre>
 * TestDataReader.loadCsvRowByColumnValue("module", "file", "scenario", "checkout", Config.environment);
 * </pre>
 *
 * <h3>Credentials policy</h3>
 * <ul>
 *   <li>Public demo credentials (e.g. SauceDemo) — safe in CSV, same across all envs</li>
 *   <li>Environment-specific test accounts — CSV with {@code environment} column</li>
 *   <li>Sensitive credentials (real passwords, API keys) — {@code parameters/system.properties} only, never in CSV</li>
 * </ul>
 */
public class TestDataReader
{

    private final Map<String, List<Map<String, String>>> sheetDataCache = new HashMap<>();
    private final String filePath;

    public TestDataReader(String filePath)
    {
        this.filePath = filePath;
    }

    // ========== EXCEL READING ==========

    public List<Map<String, String>> readExcelSheet(String sheetName)
    {
        if (sheetDataCache.containsKey(sheetName))
        {
            return sheetDataCache.get(sheetName);
        }

        List<Map<String, String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis))
        {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null)
            {
                Log.error("Sheet not found: " + sheetName + " in " + filePath);
                return data;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return data;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow)
            {
                headers.add(getCellValueAsString(cell));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++)
                {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValueAsString(cell));
                }
                data.add(rowData);
            }

            sheetDataCache.put(sheetName, data);
        }
        catch (Exception e)
        {
            Log.error("Error reading Excel file: " + e.getMessage());
        }
        return data;
    }

    public Map<String, String> readExcelRow(String sheetName, int rowIndex)
    {
        List<Map<String, String>> allData = readExcelSheet(sheetName);
        if (rowIndex >= 0 && rowIndex < allData.size())
        {
            return allData.get(rowIndex);
        }
        return new HashMap<>();
    }

    private String getCellValueAsString(Cell cell)
    {
        if (cell == null) return "";
        return switch (cell.getCellType())
        {
            case STRING -> cell.getStringCellValue();
            case NUMERIC ->
            {
                if (DateUtil.isCellDateFormatted(cell))
                {
                    yield cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA ->
            {
                try
                {
                    yield String.valueOf(cell.getNumericCellValue());
                }
                catch (Exception e)
                {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }

    // ========== CSV READING ==========

    /**
     * Read all rows from a CSV file. Placeholders in values are resolved automatically.
     *
     * @param filePath absolute or project-relative CSV path
     * @return list of rows, each row as a column-name → value map
     */
    public static List<Map<String, String>> readCsv(String filePath)
    {
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath)))
        {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return data;

            String[] headers = allRows.get(0);
            for (int i = 1; i < allRows.size(); i++)
            {
                Map<String, String> rowData = new LinkedHashMap<>();
                String[] row = allRows.get(i);
                for (int j = 0; j < headers.length; j++)
                {
                    String raw = j < row.length ? row[j].trim() : "";
                    rowData.put(headers[j].trim(), resolvePlaceholders(raw));
                }
                data.add(rowData);
            }
        }
        catch (Exception e)
        {
            Log.error("Error reading CSV file: " + e.getMessage());
        }
        return data;
    }

    /**
     * Load a single row from a CSV by matching a column value (case-insensitive).
     *
     * @param filePath    absolute CSV file path
     * @param columnName  column to search
     * @param columnValue value to match
     * @throws IllegalStateException if no matching row is found
     */
    public static Map<String, String> loadByColumn(String filePath, String columnName, String columnValue)
    {
        for (Map<String, String> row : readCsv(filePath))
        {
            String cell = row.get(columnName);
            if (cell != null && cell.equalsIgnoreCase(columnValue))
                return row;
        }
        throw new IllegalStateException(
            String.format("CSV row not found: %s='%s' in '%s'", columnName, columnValue, filePath));
    }

    /**
     * Load a single row using the module-based convention:
     * {@code src/test/resources/{module}/csvFiles/{file}.csv}
     *
     * <pre>
     * Map&lt;String, String&gt; row = TestDataReader.loadCsvRowByColumnValue(
     *     "saucedemo", "saucedemo-testdata", "scenario", "checkout");
     * </pre>
     */
    public static Map<String, String> loadCsvRowByColumnValue(
        String moduleName, String csvFileName, String columnName, String columnValue)
    {
        return loadByColumn(buildCsvPath(moduleName, csvFileName), columnName, columnValue);
    }

    /**
     * Environment-aware overload — matches both the scenario column and an {@code environment}
     * column in the same row. Use this whenever credentials or URLs differ per environment.
     *
     * <pre>
     * // CSV:  scenario, environment, username,        password
     * //       checkout, staging,     buyer_staging,   secret1
     * //       checkout, qa-1,        buyer_qa1,        secret2
     *
     * Map&lt;String, String&gt; row = TestDataReader.loadCsvRowByColumnValue(
     *     "saucedemo", "saucedemo-testdata", "scenario", "checkout", Config.environment);
     * </pre>
     *
     * @param environment value of the {@code environment} column to match (e.g. {@code Config.environment})
     */
    public static Map<String, String> loadCsvRowByColumnValue(
        String moduleName, String csvFileName, String columnName, String columnValue, String environment)
    {
        String csvPath = buildCsvPath(moduleName, csvFileName);
        for (Map<String, String> row : readCsv(csvPath))
        {
            String cell = row.get(columnName);
            String env  = row.get("environment");
            if (cell != null && cell.equalsIgnoreCase(columnValue)
                && env != null && env.equalsIgnoreCase(environment))
            {
                return row;
            }
        }
        throw new IllegalStateException(
            String.format("CSV row not found: %s='%s', environment='%s' in '%s'",
                columnName, columnValue, environment, csvPath));
    }

    /**
     * Check whether a matching row exists without throwing.
     */
    public static boolean csvRowExists(String filePath, String columnName, String columnValue)
    {
        try
        {
            loadByColumn(filePath, columnName, columnValue);
            return true;
        }
        catch (IllegalStateException e)
        {
            return false;
        }
    }

    // ========== PLACEHOLDER RESOLUTION ==========

    /**
     * Resolve dynamic placeholders in a raw CSV cell value.
     *
     * <pre>
     * {randomString:8}         — 8-char alphanumeric string
     * {randomAlpha:5}          — 5-char alphabetic string
     * {randomEmail}            — random email address
     * {randomNumber:4}         — 4-digit random number string
     * {randomUUID}             — random UUID
     * {currentDate}            — today yyyy-MM-dd
     * {currentDate:dd/MM/yyyy} — today in a custom format
     * {dateOffset:7}           — 7 days from today yyyy-MM-dd
     * {dateOffset:-1}          — yesterday yyyy-MM-dd
     * </pre>
     */
    static String resolvePlaceholders(String value)
    {
        if (value == null || !value.contains("{")) return value;

        // {randomString:N}
        value = replaceAll(value, "\\{randomString:(\\d+)\\}", m ->
            DataGenerator.randomAlphaNumericString(Integer.parseInt(m.group(1))));

        // {randomAlpha:N}
        value = replaceAll(value, "\\{randomAlpha:(\\d+)\\}", m ->
            DataGenerator.randomAlphaString(Integer.parseInt(m.group(1))));

        // {randomEmail}
        value = replaceAll(value, "\\{randomEmail\\}", m ->
            DataGenerator.randomEmail());

        // {randomNumber:N}  — N digits
        value = replaceAll(value, "\\{randomNumber:(\\d+)\\}", m -> {
            int digits = Integer.parseInt(m.group(1));
            int min = (int) Math.pow(10, digits - 1);
            int max = (int) Math.pow(10, digits) - 1;
            return String.valueOf(DataGenerator.randomNumber(min, max));
        });

        // {randomUUID}
        value = replaceAll(value, "\\{randomUUID\\}", m ->
            DataGenerator.randomUUID());

        // {currentDate:format}
        value = replaceAll(value, "\\{currentDate:([^}]+)\\}", m ->
            LocalDate.now().format(DateTimeFormatter.ofPattern(m.group(1))));

        // {currentDate}
        value = replaceAll(value, "\\{currentDate\\}", m ->
            LocalDate.now().toString());

        // {dateOffset:N}  — N days from today (negative = past)
        value = replaceAll(value, "\\{dateOffset:(-?\\d+)\\}", m ->
            LocalDate.now().plusDays(Long.parseLong(m.group(1))).toString());

        return value;
    }

    private static String replaceAll(String input, String regex, java.util.function.Function<Matcher, String> replacer)
    {
        Matcher m = Pattern.compile(regex).matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find())
            m.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(m)));
        m.appendTail(sb);
        return sb.toString();
    }

    // ========== JSON READING ==========

    public static Map<String, Object> readJson(String filePath)
    {
        try
        {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(new File(filePath),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        }
        catch (Exception e)
        {
            Log.error("Error reading JSON file: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ========== TESTNG DATAPROVIDER HELPERS ==========

    public Object[][] toDataProviderArray(String sheetName)
    {
        List<Map<String, String>> data = readExcelSheet(sheetName);
        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++)
            result[i][0] = data.get(i);
        return result;
    }

    // ========== INTERNAL HELPERS ==========

    private static String buildCsvPath(String moduleName, String csvFileName)
    {
        return String.format("%s%ssrc%stest%sresources%s%s%scsvFiles%s%s.csv",
            System.getProperty("user.dir"),
            File.separator, File.separator, File.separator, File.separator,
            moduleName, File.separator, File.separator, csvFileName);
    }
}
