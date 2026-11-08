package edu.daa.search;

import java.util.ArrayList;
import java.util.List;

public final class NaiveSearch implements StringSearch {
    @Override
    public List<Integer> search(String text, String pattern, SearchMetrics metrics) {
        int m = pattern.length();
        if (m == 0) {
            throw new IllegalArgumentException("Pattern must not be empty");
        }
        int lastStart = text.length() - m;
        if (lastStart < 0) {
            return List.of();
        }
        List<Integer> matches = new ArrayList<>();
        for (int start = 0; start <= lastStart; start++) {
            int j = 0;
            while (j < m) {
                metrics.compareInSearch();
                if (text.charAt(start + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == m) {
                matches.add(start);
            }
        }
        return matches;
    }
}
