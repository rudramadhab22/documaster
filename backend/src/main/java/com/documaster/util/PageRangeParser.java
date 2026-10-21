package com.documaster.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.documaster.exception.ApiException;
import org.springframework.http.HttpStatus;

public final class PageRangeParser {

    private static final Pattern TOKEN = Pattern.compile("^(\\d+)(?:-(\\d+))?$");

    private PageRangeParser() {
    }

    /**
     * Parses selections like "1-3,5,8-10" into 1-based page numbers.
     */
    public static Set<Integer> parse(String selection, int totalPages) {
        if (selection == null || selection.isBlank()) {
            throw new ApiException("Page selection is required.", HttpStatus.BAD_REQUEST);
        }
        if (totalPages < 1) {
            throw new ApiException("PDF has no pages.", HttpStatus.BAD_REQUEST);
        }

        Set<Integer> pages = new LinkedHashSet<>();
        String[] parts = selection.split(",");
        for (String raw : parts) {
            String part = raw.trim();
            if (part.isEmpty()) {
                continue;
            }
            var matcher = TOKEN.matcher(part);
            if (!matcher.matches()) {
                throw new ApiException(
                        "Invalid page selection '" + part + "'. Use formats like 1-3,5,8-10.",
                        HttpStatus.BAD_REQUEST);
            }
            int start = Integer.parseInt(matcher.group(1));
            int end = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : start;
            if (start < 1 || end < 1 || start > totalPages || end > totalPages) {
                throw new ApiException(
                        "Page numbers must be between 1 and " + totalPages + ".",
                        HttpStatus.BAD_REQUEST);
            }
            if (start > end) {
                throw new ApiException(
                        "Invalid range '" + part + "': start cannot be greater than end.",
                        HttpStatus.BAD_REQUEST);
            }
            for (int i = start; i <= end; i++) {
                pages.add(i);
            }
        }

        if (pages.isEmpty()) {
            throw new ApiException("No valid pages selected.", HttpStatus.BAD_REQUEST);
        }
        return pages;
    }
}
