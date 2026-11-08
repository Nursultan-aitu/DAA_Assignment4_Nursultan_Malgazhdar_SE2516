package edu.daa.edit;

public final class TwoRowEditDistance implements EditDistance {
    // State: previous and current hold consecutive rows of the prefix-distance table.
    // Transition: current[j] is the minimum of previous[j] + 1, current[j-1] + 1, and previous[j-1] + cost.
    // Base: previous[j] = j at row 0 and current[0] = i at row i.
    @Override
    public int distance(String a, String b, EditMetrics metrics) {
        int n = a.length();
        int m = b.length();
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        metrics.allocateBuffer(2L * (m + 1L));
        for (int j = 0; j <= m; j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            current[0] = i;
            for (int j = 1; j <= m; j++) {
                metrics.compareCharacters();
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(previous[j - 1] + cost,
                        1 + Math.min(previous[j], current[j - 1]));
                metrics.fillCell();
            }
            int[] oldPrevious = previous;
            previous = current;
            current = oldPrevious;
        }
        return previous[m];
    }
}
