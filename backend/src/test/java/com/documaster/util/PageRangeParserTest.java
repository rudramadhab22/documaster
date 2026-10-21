package com.documaster.util;

import java.util.Set;

import com.documaster.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageRangeParserTest {

    @Test
    void parsesMixedRangesAndSingles() {
        Set<Integer> pages = PageRangeParser.parse("1-3,5,8-10", 12);
        assertEquals(Set.of(1, 2, 3, 5, 8, 9, 10), pages);
    }

    @Test
    void rejectsOutOfBounds() {
        ApiException ex = assertThrows(ApiException.class, () -> PageRangeParser.parse("1-5", 3));
        assertTrue(ex.getMessage().contains("between 1 and 3"));
    }

    @Test
    void rejectsInvalidToken() {
        assertThrows(ApiException.class, () -> PageRangeParser.parse("1-3,abc", 10));
    }

    @Test
    void rejectsInvertedRange() {
        assertThrows(ApiException.class, () -> PageRangeParser.parse("5-2", 10));
    }
}
