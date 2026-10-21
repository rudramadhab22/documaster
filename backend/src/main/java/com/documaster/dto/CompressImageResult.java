package com.documaster.dto;

public record CompressImageResult(
        byte[] data,
        long originalSize,
        long compressedSize,
        String contentType,
        String filename,
        boolean reduced,
        String message
) {
}
