package com.yourcompany.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelReader implements AutoCloseable {
    private Workbook workbook;
    private Sheet sheet;
    private FileInputStream file;
    
    public ExcelReader(String filePath, String sheetName) {
        try {
            FileInputStream file = new FileInputStream(new File(filePath));
            workbook = new XSSFWorkbook(file);
            
            sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("Sheet '" + sheetName + "' not found, creating empty sheet");
                sheet = workbook.createSheet(sheetName);
                Row headerRow = sheet.createRow(0);
                if ("LoginData".equals(sheetName)) {
                    headerRow.createCell(0).setCellValue("TestCase");
                    headerRow.createCell(1).setCellValue("Username");
                    headerRow.createCell(2).setCellValue("Password");
                } else if ("CartData".equals(sheetName)) {
                    headerRow.createCell(0).setCellValue("TestCase");
                    headerRow.createCell(1).setCellValue("ItemCount");
                }
                FileOutputStream out = new FileOutputStream(filePath);
                workbook.write(out);
                out.close();
            }
            
            System.out.println("Excel loaded: " + sheetName);
        } catch (Exception e) {
            System.out.println("Excel Error: " + e.getMessage());
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(sheetName);
        }
    }
    
    public String getData(String testCase, String columnName, String sheetName) {
        try {
            if (!sheet.getSheetName().equals(sheetName)) {
                sheet = workbook.getSheet(sheetName);
            }
            
            Row headerRow = sheet.getRow(0);
            int columnIndex = -1;
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                    columnIndex = i;
                    break;
                }
            }
            
            if (columnIndex == -1) {
                System.out.println("Column not found: " + columnName);
                return null;
            }
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell testCaseCell = row.getCell(0);
                    if (testCaseCell != null && testCaseCell.getStringCellValue().equalsIgnoreCase(testCase)) {
                        Cell dataCell = row.getCell(columnIndex);
                        return dataCell != null ? dataCell.toString() : null;
                    }
                }
            }
            
            System.out.println("Test case not found: " + testCase);
            return null;
            
        } catch (Exception e) {
            System.out.println("Excel read error: " + e.getMessage());
            return null;
        }
    }
    
    public Map<String, String> getTestData(String testCase) {
        Map<String, String> data = new HashMap<>();
        try {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell testCaseCell = row.getCell(0);
                    if (testCaseCell != null && testCaseCell.getStringCellValue().equalsIgnoreCase(testCase)) {
                        Row headerRow = sheet.getRow(0);
                        for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                            String columnName = headerRow.getCell(j).getStringCellValue();
                            Cell cell = row.getCell(j);
                            String value = cell != null ? cell.toString() : "";
                            data.put(columnName, value);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Excel getTestData error: " + e.getMessage());
        }
        return data;
    }
    
    public String getData(String testCase, String columnName) {
        return getData(testCase, columnName, sheet.getSheetName());
    }
    
    @Override
    public void close() {
        try {
            if (workbook != null) {
                workbook.close();
            }
            if (file != null) {
                file.close();
            }
            System.out.println("Excel resources closed");
        } catch (Exception e) {
            System.out.println("Error closing Excel: " + e.getMessage());
        }
    }
}