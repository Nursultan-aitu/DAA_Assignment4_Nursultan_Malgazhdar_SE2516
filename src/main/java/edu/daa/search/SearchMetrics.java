package edu.daa.search;

/** Counts actual character comparisons in preprocessing and search. */
public final class SearchMetrics {
    private long lpsComparisons;
    private long searchComparisons;

    void compareInLps() {
        lpsComparisons++;
    }

    void compareInSearch() {
        searchComparisons++;
    }

    public long lpsComparisons() {
        return lpsComparisons;
    }

    public long searchComparisons() {
        return searchComparisons;
    }
}
