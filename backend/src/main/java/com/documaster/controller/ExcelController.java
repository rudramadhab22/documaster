package com.documaster.controller;

import com.documaster.service.ExcelService;
import com.documaster.util.FileValidation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/to-pdf")
    public ResponseEntity<byte[]> toPdf(@RequestParam("file") MultipartFile file) {
        FileValidation.requireExcel(file);
        ExcelService.ProcessedFile result = excelService.excelToPdf(file);
        return fileResponse(result);
    }

    @PostMapping("/to-csv")
    public ResponseEntity<byte[]> toCsv(@RequestParam("file") MultipartFile file) {
        FileValidation.requireExcel(file);
        ExcelService.ProcessedFile result = excelService.excelToCsv(file);
        return fileResponse(result);
    }

    @PostMapping("/from-csv")
    public ResponseEntity<byte[]> fromCsv(@RequestParam("file") MultipartFile file) {
        FileValidation.requireCsv(file);
        ExcelService.ProcessedFile result = excelService.csvToExcel(file);
        return fileResponse(result);
    }

    @PostMapping("/to-images")
    public ResponseEntity<byte[]> toImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", defaultValue = "png") String format) {
        FileValidation.requireExcel(file);
        ExcelService.ProcessedFile result = excelService.excelToImages(file, format);
        return fileResponse(result);
    }

    @PostMapping("/to-pdfs-per-sheet")
    public ResponseEntity<byte[]> toPdfsPerSheet(@RequestParam("file") MultipartFile file) {
        FileValidation.requireExcel(file);
        ExcelService.ProcessedFile result = excelService.excelToPdfsPerSheet(file);
        return fileResponse(result);
    }

    private ResponseEntity<byte[]> fileResponse(ExcelService.ProcessedFile result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }
}
