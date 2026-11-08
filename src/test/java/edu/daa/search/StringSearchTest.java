package edu.daa.search;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringSearchTest {
    private final StringSearch naive = new NaiveSearch();
    private final StringSearch kmp = new KmpSearch();

    @Test
    void buildsReferenceLpsArrays() {
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, KmpSearch.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, KmpSearch.lps("ABABCABAB"));
    }

    @Test
    void findsReferenceMatchesIncludingOverlaps() {
        check(List.of(10), "ABABDABACDABABCABAB", "ABABCABAB");
        check(List.of(0, 1, 2, 3), "AAAAA", "AA");
        check(List.of(0, 2, 4), "ABABABA", "ABA");
    }

    @Test
    void findsMatchesAtBothEndsAndForSingleCharacters() {
        check(List.of(0, 4), "abxyab", "ab");
        check(List.of(0, 1, 3), "aaba", "a");
    }

    @Test
    void returnsAnEmptyListWhenNoMatchIsPossible() {
        check(List.of(), "AAAA", "AB");
        check(List.of(), "a", "abcd");
        check(List.of(), "", "a");
    }

    @Test
    void rejectsAnEmptyPattern() {
        for (StringSearch search : List.of(naive, kmp)) {
            assertThrows(IllegalArgumentException.class, () -> search.search("abc", ""));
            assertThrows(IllegalArgumentException.class, () -> search.search("", ""));
        }
    }

    @Test
    void agreesOnOneHundredSeededTexts() {
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            String text = word(random, random.nextInt(61));
            String pattern;
            if (i % 2 == 0 && !text.isEmpty()) {
                int start = random.nextInt(text.length());
                pattern = text.substring(start, start + 1 + random.nextInt(text.length() - start));
            } else {
                pattern = word(random, 1 + random.nextInt(12));
            }
            assertEquals(naive.search(text, pattern), kmp.search(text, pattern),
                    () -> "text=" + text + ", pattern=" + pattern);
        }
    }

    @Test
    void countsLateMismatchesAndLpsPreprocessingSeparately() {
        SearchMetrics naiveMetrics = new SearchMetrics();
        SearchMetrics kmpMetrics = new SearchMetrics();
        assertEquals(List.of(), naive.search("AAAA", "AB", naiveMetrics));
        assertEquals(List.of(), kmp.search("AAAA", "AB", kmpMetrics));
        assertEquals(6, naiveMetrics.searchComparisons());
        assertEquals(0, naiveMetrics.lpsComparisons());
        assertEquals(7, kmpMetrics.searchComparisons());
        assertEquals(1, kmpMetrics.lpsComparisons());
    }

    private void check(List<Integer> expected, String text, String pattern) {
        assertEquals(expected, naive.search(text, pattern));
        assertEquals(expected, kmp.search(text, pattern));
    }

    private static String word(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append((char) ('A' + random.nextInt(3)));
        }
        return result.toString();
    }
}
