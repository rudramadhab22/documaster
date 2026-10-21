package com.documaster.dto;

public record CompressPdfResult(
        byte[] data,
        long originalSize,
        long compressedSize,
        boolean reduced,
        String message
) {
}
