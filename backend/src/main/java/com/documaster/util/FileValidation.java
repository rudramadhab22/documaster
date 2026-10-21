package com.documaster.util;

import java.util.Locale;
import java.util.Set;

import com.documaster.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

public final class FileValidation {

    private static final Set<String> PDF_TYPES = Set.of("application/pdf");
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    private FileValidation() {
    }

    public static void requirePresent(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(label + " is required.", HttpStatus.BAD_REQUEST);
        }
    }

    public static void requirePdf(MultipartFile file) {
        requirePresent(file, "PDF file");
        String contentType = normalize(file.getContentType());
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        if (!PDF_TYPES.contains(contentType) && !name.endsWith(".pdf")) {
            throw new ApiException("Only PDF files are allowed.", HttpStatus.BAD_REQUEST);
        }
    }

    public static void requireImage(MultipartFile file) {
        requirePresent(file, "Image file");
        String contentType = normalize(file.getContentType());
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        boolean okType = IMAGE_TYPES.contains(contentType);
        boolean okExt = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
        if (!okType && !okExt) {
            throw new ApiException("Only JPG and PNG images are allowed.", HttpStatus.BAD_REQUEST);
        }
    }

    public static void requirePdfs(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new ApiException("At least one PDF file is required.", HttpStatus.BAD_REQUEST);
        }
        for (MultipartFile file : files) {
            requirePdf(file);
        }
    }

    public static void requireImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new ApiException("At least one image is required.", HttpStatus.BAD_REQUEST);
        }
        for (MultipartFile file : files) {
            requireImage(file);
        }
    }

    private static String normalize(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
    }
}
