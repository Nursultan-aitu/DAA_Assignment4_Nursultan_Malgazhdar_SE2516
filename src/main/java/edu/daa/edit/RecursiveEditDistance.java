package edu.daa.edit;

public final class RecursiveEditDistance implements EditDistance {
    // State: d(i, j) is the distance between prefixes of lengths i and j.
    // Transition: keep equal last characters; otherwise take 1 + min(delete, insert, replace).
    // Base: d(0, j) = j and d(i, 0) = i.
    @Override
    public int distance(String a, String b, EditMetrics metrics) {
        return distance(a, b, a.length(), b.length(), metrics);
    }

    private int distance(String a, String b, int i, int j, EditMetrics metrics) {
        metrics.visitRecursiveCall();
        if (i == 0) {
            return j;
        }
        if (j == 0) {
            return i;
        }
        metrics.compareCharacters();
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return distance(a, b, i - 1, j - 1, metrics);
        }
        int delete = distance(a, b, i - 1, j, metrics);
        int insert = distance(a, b, i, j - 1, metrics);
        int replace = distance(a, b, i - 1, j - 1, metrics);
        return 1 + Math.min(delete, Math.min(insert, replace));
    }
}
