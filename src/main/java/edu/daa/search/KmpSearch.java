package edu.daa.search;

import java.util.ArrayList;
import java.util.List;

public final class KmpSearch implements StringSearch {
    /** LPS[i] is the longest proper prefix of pattern[0..i] that is also its suffix. */
    public static int[] lps(String pattern) {
        return lps(pattern, new SearchMetrics());
    }

    private static int[] lps(String pattern, SearchMetrics metrics) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            metrics.compareInLps();
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i++] = ++length;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                i++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern, SearchMetrics metrics) {
        int m = pattern.length();
        if (m == 0) {
            throw new IllegalArgumentException("Pattern must not be empty");
        }
        int n = text.length();
        if (m > n) {
            return List.of();
        }
        int[] lps = lps(pattern, metrics);
        List<Integer> matches = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < n) {
            metrics.compareInSearch();
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    matches.add(i - m);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }
}
