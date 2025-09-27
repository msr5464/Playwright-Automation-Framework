package helpers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class TestDataReader {

    private ArrayList<List<String>> excelData;
    private Config config;

    public TestDataReader(Config config, String sheetName, String excelFilePath) {
        readFile(config, sheetName, excelFilePath);
    }

    public TestDataReader(Config config) {
        this.config = config;
    }

    /**
     * This method is to read a csv file and return it as List of String Array
     *
     * @param filePath - file path of csv
     * @return - List<String[]> of data
     */
    public static List<String[]> getCompleteCsvData(String filePath) {
        CSVReader csvReader = null;
        List<String[]> csvBody = null;
        try {
            csvReader = new CSVReader(new FileReader(filePath));
            csvBody = csvReader.readAll();
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return csvBody;
    }

    /**
     * This function is used to read the excel sheets of type .xls, .xlsx and .csv
     *
     * @param sheetName
     * @param filePath
     */
    private void readFile(Config config, String sheetName, String filePath) {
        String filename = filePath.trim();
        BufferedReader csvFile = null;
        FileInputStream fileInputStream = null;
        excelData = new ArrayList<List<String>>();
        config.logCommentForDebugging("Read:-'" + filePath + "', Sheet:- '" + sheetName + "'");
        try {
            fileInputStream = new FileInputStream(filename);
            if (filename.endsWith(".csv")) {
                csvFile = new BufferedReader(
                        new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
                String dataRow = csvFile.readLine();

                while (dataRow != null) {
                    String[] dataArray = dataRow.split(",");
                    List<String> data = new ArrayList<String>();
                    int counter = 0;
                    String tempStr = "";
                    for (int z = 0; z < dataArray.length; z++) {
                        String str = dataArray[z];
                        boolean sameLoop = false;
                        if (str.startsWith("\"")) {
                            str = str.replace("\"", "");
                            counter++;
                            tempStr = str;
                            sameLoop = true;
                        }
                        if (str.endsWith("\"")) {
                            str = str.replace("\"", "");
                            counter = 0;
                            if (sameLoop) {
                                tempStr = str;
                            } else {
                                tempStr = tempStr + "," + str;
                            }
                        }

                        if (counter > 0) {
                            if (!tempStr.equals(str)) {
                                tempStr = tempStr + "," + str;
                            }
                        } else {
                            if (StringUtils.isEmpty(tempStr)) {
                                tempStr = str;
                            }
                            data.add(tempStr);
                            tempStr = "";
                        }
                    }
                    excelData.add(data);
                    dataRow = csvFile.readLine();
                }
            } else if (filename.endsWith(".mdt")) {
                csvFile = new BufferedReader(new FileReader(filePath));
                String dataRow = csvFile.readLine();

                while (dataRow != null) {
                    String[] dataArray = dataRow.split("\\|");
                    List<String> data = new ArrayList<String>();
                    for (int z = 0; z < dataArray.length; z++) {
                        String str = dataArray[z];
                        if (str.startsWith("\"")) {
                            str = str.replace("\"", "");
                        }
                        data.add(str);
                    }
                    excelData.add(data);
                    dataRow = csvFile.readLine();
                }
            }
        } catch (FileNotFoundException e) {
            config.logExceptionAndFail(e);
        } catch (IOException e) {
            config.logExceptionAndFail(e);
        } catch (Exception e) {
            config.logExceptionAndFail(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    config.logExceptionAndFail(e);
                }
            }

            if (csvFile != null) {
                try {
                    csvFile.close();
                } catch (IOException e) {
                    config.logExceptionAndFail(e);
                }
            }
        }
    }

    /**
     * This function is used to fetch the data of a particular 'cell' of excel sheet
     *
     * @param config - Pass object of config
     * @param row    - row number of sheet
     * @param column - coumn name of sheet
     * @return - string value
     */
    public String getData(Config config, int row, String column) {
        String data = "";
        List<String> headerRow = excelData.get(0);
        List<String> dataRow = excelData.get(row);

        for (int i = 0; i < headerRow.size(); i++) {
            if (headerRow.get(i).equalsIgnoreCase(column)) {
                try {
                    data = dataRow.get(i);
                } catch (IndexOutOfBoundsException e) {
                    data = "";
                }
                break;
            }
        }

        if (data.equals("")) {
            data = "{skip}";
        } else {
            if (data.contains("{empty}")) {
                data = data.replace("{empty}", "");
            }
            if (data.contains("{space}")) {
                data = data.replace("{space}", " ");
            }
            if (data.contains("{currentDateTime}")) {
                data = data.replace("{currentDateTime}", DataGenerator.getCurrentDateTime("YYYY-MM-dd hh:mm:ss ZZZZ"));
            }
            if (data.contains("randomUUID")) {
                UUID uuid = UUID.randomUUID();
                data = data.replace("{randomUUID}", uuid.toString());
            }

            if (data.contains("{randomDecimalNum:")) {
                int start = data.indexOf("Num:") + 4;
                int end = data.indexOf("}", start);
                String substr = data.substring(start, end);
                String[] values = substr.split(",");
                int lowerBound = Integer.parseInt(values[0]);
                int upperBound = Integer.parseInt(values[1]);
                int precision = Integer.parseInt(values[2]);
                // {randomDecimalNum:0,10,2}
                data = data.replace("{randomDecimalNum:" + lowerBound + "," + upperBound + "," + precision + "}",
                        DataGenerator.generateRandomDecimalValue(lowerBound, upperBound, precision));
            }

            while (data.contains("{random")) {
                if (data.contains("{randomString:")) {
                    int start = data.indexOf("String:") + 7;
                    int end = data.indexOf("}", start);
                    int length = Integer.parseInt(data.substring(start, end));
                    data = data.replace("{randomString:" + length + "}", DataGenerator.generateRandomString(length));
                } else {
                    int start = data.indexOf("Num:") + 4;
                    int end = data.indexOf("}", start);
                    int length = Integer.parseInt(data.substring(start, end));
                    if (data.contains("{randomAlphaNum:" + length + "}")) {
                        data = data.replace("{randomAlphaNum:" + length + "}",
                                DataGenerator.generateRandomAlphaNumericString(length));
                    }
                    if (data.contains("{randomNum:" + length + "}")) {
                        data = data.replace("{randomNum:" + length + "}",
                                Long.toString(DataGenerator.generateRandomNumber(length)));
                    }
                }
            }
        }

        config.logCommentForDebugging("Value of '" + column + "' column at row " + row + " is:- '" + data + "'");
        return data;
    }

    /**
     * This method returns the number of records present in the datasheet
     *
     * @return number of records
     */
    public int getRecordsNum() {
        return excelData.size();
    }

    /**
     * This method returns the number of columns of the datasheet (It counts the
     * header and returns the number)
     *
     * @return number of columns
     */
    public int getColumnNum() {
        List<String> headerRow = excelData.get(0);
        return headerRow.size();
    }

    /**
     * Returns the Excel header value
     *
     * @param rowNumber - Excel Row number to read
     * @return The value read
     */
    public String getHeaderData(int rowNumber) {
        String data = "";
        List<String> dataRow = excelData.get(0);
        try {
            data = dataRow.get(rowNumber);
        } catch (IndexOutOfBoundsException e) {
            data = "";
        }
        data = data.trim();
        return data;
    }

    /**
     * @param config - object of confing
     * @param rowNum - row number of sheet
     * @return Hashmap - testdata in hashmap
     */
    public HashMap<String, String> getTestData(Config config, int rowNum) {
        HashMap<String, String> testDataMap = new HashMap<>();
        for (int i = 0; i < excelData.get(0).size(); i++) {
            String key = excelData.get(0).get(i);
            String value = getData(config, rowNum, excelData.get(0).get(i));
            if (value.equals("{skip}")) {
                if (config.testData.get(key) != null) {
                    config.testData.remove(key);
                }
                continue;
            }

            value = config.replaceArgumentsWithRunTimeProperties(value);
            testDataMap.put(key, value);
        }
        config.logComment("Test Data:- " + testDataMap);
        testDataMap.remove("RowNo");

        return testDataMap;
    }

    /**
     * This method will read data for row index rowNum and having header at row
     * `headerRowNum`
     *
     * @param config       - object of confing
     * @param rowNum       - row number of sheet
     * @param headerRowNum - row number of headers in sheet
     * @return Hashmap - testdata in hashmap
     */
    public HashMap<String, String> getTestData(Config config, int rowNum, int headerRowNum) {
        HashMap<String, String> testDataMap = new HashMap<>();
        for (int i = 0; i < excelData.get(headerRowNum).size(); i++) {
            String key = excelData.get(headerRowNum).get(i);
            String value = null;
            if (i < excelData.get(rowNum).size()) {
                value = excelData.get(rowNum).get(i);
            }
            if (value == null || value.equals("{skip}")) {
                if (config.testData.get(key) != null) {
                    config.testData.remove(key);
                }
                continue;
            }

            value = config.replaceArgumentsWithRunTimeProperties(value);
            testDataMap.put(key, value);
        }
        return testDataMap;
    }

    /**
     * Method to update the csv file on the specified row and column
     *
     * @param config              - object of config
     * @param filePath            - path of csv file
     * @param testDataToBeUpdated - testdata in string
     * @param testDataRowNo       - sheet row number
     * @param testDataColumnNo    - sheet column number
     */
    public static void updateTestDataInCsvFile(Config config, String filePath, String testDataToBeUpdated,
            int testDataRowNo, int testDataColumnNo, boolean withLine) {
        CSVReader reader = null;
        List<String[]> csvBody = null;
        try {
            reader = new CSVReader(new FileReader(filePath));
            csvBody = reader.readAll();
            csvBody.get(testDataRowNo)[testDataColumnNo] = testDataToBeUpdated;
            reader.close();
        } catch (Exception e) {
            config.logException("Exception while reading csv file", e);
        }

        CSVWriter writer;
        try {
            if (withLine) {
                writer = new CSVWriter(new FileWriter(filePath));
            } else {
                writer = new CSVWriter(new FileWriter(filePath), CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            }
            writer.writeAll(csvBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            config.logException("File could not be closed, the exception is ", e);
        }
    }

    /**
     * This method is to read a csv file and return it as HashMap in form of key
     * values
     *
     * @param filePath - file path of csv
     * @return - Hashmap of data
     */
    public HashMap<String, String> getDataFromCsv(String filePath) {
        String key;
        String value;
        CSVReader csvReader = null;
        List<String[]> csvBody = null;
        HashMap<String, String> csvData = new HashMap<>();
        try {
            csvReader = new CSVReader(new FileReader(filePath));
            csvBody = csvReader.readAll();
            if (csvBody.size() >= 2) {
                for (int i = 0; i < csvBody.get(0).length; i++) {
                    key = csvBody.get(0)[i].replace(" ", "");
                    value = csvBody.get(1)[i];
                    if (value.isEmpty()) {
                        value = "{skip}";
                    }
                    csvData.put(key, value);
                }
            }
            csvReader.close();
        } catch (Exception e) {
            config.logException("Exception while reading csv file", e);
        }
        return csvData;
    }

    public static void modifyDataOfColumnInCsvCell(Config config, String filePath, int rowToModify, int columnToModify,
            String dataToBeWrite) {
        String csvFilePath = filePath;
        try {
            FileReader fileReader = new FileReader(csvFilePath);
            CSVReader csvReader = new CSVReader(fileReader);
            List<String[]> csvData = csvReader.readAll();
            csvReader.close();
            if (rowToModify <= csvData.size()) {
                String[] row = csvData.get(rowToModify);
                if (columnToModify <= row.length) {
                    row[columnToModify] = dataToBeWrite;
                }
            }
            FileWriter fileWriter = new FileWriter(csvFilePath);
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            csvWriter.writeAll(csvData);
            csvWriter.close();
        } catch (Exception e) {
            config.logFail("Unable to modify the data of specified file, please check the parameters");
        }
    }

    public static void removeDoubleQuotesFromCsv(Config config, String file1Path, String file2Path) {
        String csvFilePath = file1Path;
        String csvFilePathDest = file2Path;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePathDest))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                for (int i = 0; i < fields.length; i++) {
                    // Remove double quotes from each field
                    fields[i] = fields[i].replaceAll("\"\"", "A2P1R3");
                    fields[i] = fields[i].replaceAll("\"", "");
                    fields[i] = fields[i].replaceAll("A2P1R3", "\"");
                }
                String modifiedLine = String.join(",", fields);
                writer.write(modifiedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            config.logFail("Unable to remove double quotes from specified file, please check the parameters");
        }
    }

    public static String readDataFromCsv(Config config, String csvFilePath, String columnName, int rowIndex) {
        String cellValue = null;
        try {
            cellValue = fetchCsvValueWithMutipleHeaderLines(csvFilePath, columnName, rowIndex);
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            config.logFail("Unable to read data from csv file " + e.getMessage());
        }
        return cellValue;
    }

    private static String fetchCsvValueWithMutipleHeaderLines(String csvFilePath, String columnName, int rowIndex)
            throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            String[] columnNames = csvReader.readNext();

            // Find the index of the specified column
            int columnIndex = -1;
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equalsIgnoreCase(columnName)) {
                    columnIndex = i;
                    break;
                }
            }
            if (columnIndex == -1) {
                throw new IllegalArgumentException("Column '" + columnName + "' not found in the CSV file.");
            }
            // Read the specified row and return the value
            String[] row = null;
            for (int i = 0; i < rowIndex; i++) {
                row = csvReader.readNext();
                if (row == null) {
                    throw new IllegalArgumentException(
                            "Row index " + rowIndex + " exceeds the number of rows in the CSV file.");
                }
            }
            if (rowIndex == 0) {
                throw new IllegalArgumentException("Row index must be greater than 0.");
            }
            if (columnIndex >= row.length) {
                throw new IllegalArgumentException("Column index exceeds the number of columns in the CSV file.");
            }
            return row[columnIndex];
        }
    }

    /**
     * This program will check if a csv file has multiline header and then replace a
     * specific column with
     * unique 10 character random alphanumeric string
     */
    public static void modifyDataInCsvFileWithMutipleHeaderLines(Config config, String csvFileName,
            String columnNameToReplace) {
        int randomStringLength = 10;
        try {
            createRandomValuesInCsvForASpecificColumnByName(config, csvFileName, columnNameToReplace,
                    randomStringLength);
        } catch (IOException e) {
            config.logFail(e.getMessage());
        }
    }

    private static void createRandomValuesInCsvForASpecificColumnByName(Config config, String filePath,
            String columnNameToReplace, int randomStringLength) throws IOException {
        // Create a temporary file
        String tempFilePath = filePath + ".tmp";
        CSVReader csvReader = new CSVReader(new FileReader(filePath));
        CSVWriter csvWriter = new CSVWriter(new FileWriter(tempFilePath));
        Set<String> generatedStrings = new HashSet<>();
        try {
            // Read and write the header
            String[] header = csvReader.readNext();
            csvWriter.writeNext(header);

            // Find the index of the specified column by name
            int columnIndexToReplace = findColumnIndex(header, columnNameToReplace);

            if (columnIndexToReplace == -1) {
                throw new IllegalArgumentException("Column with name " + columnNameToReplace + " not found.");
            }

            // Iterate over the records and replace the specified column with unique random
            // alphanumeric strings
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                String randomValue;
                do {
                    randomValue = DataGenerator.generateRandomAlphaNumericString(randomStringLength);
                } while (!generatedStrings.add(randomValue));

                // Set the random string as the new value for the specified column
                record[columnIndexToReplace] = randomValue;

                // Write the updated record to the temporary file
                csvWriter.writeNext(record);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        } finally {
            csvReader.close();
            csvWriter.close();
        }
        // Replace the original file with the temporary file
        replaceOriginalFile(config, filePath, tempFilePath);
    }

    /**
     * Function to find the index of a column in the header
     */
    private static int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1; // Column not found
    }

    /**
     * Function to replace the original file with the temporary file
     */
    private static void replaceOriginalFile(Config config, String originalFilePath, String tempFilePath) {
        try {
            // Delete the original file
            if (!new java.io.File(originalFilePath).delete()) {
                throw new IOException("Could not delete the original file.");
            }

            // Rename the temporary file to the original file
            if (!new java.io.File(tempFilePath).renameTo(new java.io.File(originalFilePath))) {
                throw new IOException("Could not rename the temporary file.");
            }
        } catch (IOException e) {
            config.logFail(e.getMessage());
        }
    }

    public static String[] getColumnNames(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine(); // Read the first line (header)
        reader.close();

        if (line == null) {
            return new String[0]; // Empty file or missing header
        }

        return line.split(","); // Split the header line by delimiter (comma in this case)
    }
}