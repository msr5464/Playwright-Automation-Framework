package automation.core;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import automation.core.Log;

import java.io.*;
import java.util.*;

/**
 * Multi-source test data reader supporting Excel and CSV files.
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
                    rowData.put(headers[j].trim(), j < row.length ? row[j].trim() : "");
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

    // ========== JSON READING ==========

    public static Map<String, Object> readJson(String filePath)
    {
        try
        {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(new File(filePath), Map.class);
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
        {
            result[i][0] = data.get(i);
        }
        return result;
    }
}
