package com.documaster.controller;

import java.util.Map;

import com.documaster.dto.CompressPdfResult;
import com.documaster.exception.ApiException;
import com.documaster.service.PdfService;
import com.documaster.util.FileValidation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/compress")
    public ResponseEntity<?> compress(@RequestParam("file") MultipartFile file) {
        FileValidation.requirePdf(file);
        CompressPdfResult result = pdfService.compress(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressed.pdf\"")
                .header("X-Original-Size", String.valueOf(result.originalSize()))
                .header("X-Compressed-Size", String.valueOf(result.compressedSize()))
                .header("X-Reduced", String.valueOf(result.reduced()))
                .header("X-Message", result.message())
                .contentType(MediaType.APPLICATION_PDF)
                .body(result.data());
    }

    @PostMapping("/image-to-pdf")
    public ResponseEntity<byte[]> imageToPdf(@RequestParam("files") MultipartFile[] files) {
        FileValidation.requireImages(files);
        byte[] pdf = pdfService.imagesToPdf(files);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"images.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> merge(@RequestParam("files") MultipartFile[] files) {
        FileValidation.requirePdfs(files);
        if (files.length < 2) {
            throw new ApiException("At least two PDF files are required to merge.", HttpStatus.BAD_REQUEST);
        }
        byte[] pdf = pdfService.merge(files);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merged.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/split")
    public ResponseEntity<byte[]> split(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") String pages) {
        FileValidation.requirePdf(file);
        byte[] pdf = pdfService.split(file, pages);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"split.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/page-count")
    public ResponseEntity<Map<String, Integer>> pageCount(@RequestParam("file") MultipartFile file) {
        FileValidation.requirePdf(file);
        int count = pdfService.pageCount(file);
        return ResponseEntity.ok(Map.of("pageCount", count));
    }

    @PostMapping("/to-images")
    public ResponseEntity<byte[]> toImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") String pages,
            @RequestParam(value = "format", defaultValue = "png") String format) {
        FileValidation.requirePdf(file);
        PdfService.ProcessedFile result = pdfService.toImages(file, pages, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }
}
