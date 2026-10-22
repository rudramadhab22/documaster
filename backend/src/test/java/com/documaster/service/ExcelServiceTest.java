package com.documaster.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelServiceTest {

    private final ExcelService excelService = new ExcelService();

    @Test
    void excelToPdfCreatesDocument() throws Exception {
        MockMultipartFile file = excelFile();
        var result = excelService.excelToPdf(file);
        assertEquals("application/pdf", result.contentType());
        try (PDDocument document = Loader.loadPDF(result.data())) {
            assertTrue(document.getNumberOfPages() >= 1);
        }
    }

    @Test
    void excelToCsvExportsData() throws Exception {
        MockMultipartFile file = excelFile();
        var result = excelService.excelToCsv(file);
        String csv = new String(result.data(), StandardCharsets.UTF_8);
        assertTrue(csv.contains("Name"));
        assertTrue(csv.contains("Alice"));
    }

    @Test
    void csvToExcelCreatesWorkbook() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file", "data.csv", "text/csv", "A,B\n1,2\n".getBytes(StandardCharsets.UTF_8));
        var result = excelService.csvToExcel(csv);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result.data()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("A", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    void excelToImagesReturnsPng() throws Exception {
        MockMultipartFile file = excelFile();
        var result = excelService.excelToImages(file, "png");
        assertEquals("image/png", result.contentType());
        assertTrue(result.data().length > 0);
    }

    @Test
    void excelToPdfsPerSheetZipsMultipleSheets() throws Exception {
        MockMultipartFile file = multiSheetExcel();
        var result = excelService.excelToPdfsPerSheet(file);
        assertEquals("application/zip", result.contentType());
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(result.data()))) {
            assertTrue(zip.getNextEntry() != null);
            assertTrue(zip.getNextEntry() != null);
        }
    }

    private MockMultipartFile excelFile() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("People");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Alice");
            row.createCell(1).setCellValue(30);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return new MockMultipartFile(
                    "file",
                    "people.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    baos.toByteArray());
        }
    }

    private MockMultipartFile multiSheetExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet a = workbook.createSheet("Alpha");
            a.createRow(0).createCell(0).setCellValue("A1");
            Sheet b = workbook.createSheet("Beta");
            b.createRow(0).createCell(0).setCellValue("B1");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return new MockMultipartFile(
                    "file",
                    "multi.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    baos.toByteArray());
        }
    }
}
