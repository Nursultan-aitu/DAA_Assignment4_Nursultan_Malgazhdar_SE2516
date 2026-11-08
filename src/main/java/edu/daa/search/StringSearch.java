package edu.daa.search;

import java.util.List;

public interface StringSearch {
    default List<Integer> search(String text, String pattern) {
        return search(text, pattern, new SearchMetrics());
    }

    List<Integer> search(String text, String pattern, SearchMetrics metrics);
}
