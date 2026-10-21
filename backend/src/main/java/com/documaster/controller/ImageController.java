package com.documaster.controller;

import com.documaster.dto.CompressImageResult;
import com.documaster.service.ImageService;
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
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compress(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", defaultValue = "0.7") float quality) {
        FileValidation.requireImage(file);
        CompressImageResult result = imageService.compress(file, quality);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .header("X-Original-Size", String.valueOf(result.originalSize()))
                .header("X-Compressed-Size", String.valueOf(result.compressedSize()))
                .header("X-Reduced", String.valueOf(result.reduced()))
                .header("X-Message", result.message())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }
}
