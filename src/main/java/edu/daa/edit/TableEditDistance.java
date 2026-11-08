package edu.daa.edit;

public final class TableEditDistance implements EditDistance {
    // State: dp[i][j] stores the distance between prefixes of lengths i and j.
    // Transition: take the minimum of up + 1, left + 1, and diagonal + mismatch cost.
    // Base: dp[i][0] = i and dp[0][j] = j.
    @Override
    public int distance(String a, String b, EditMetrics metrics) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        metrics.allocateBuffer((n + 1L) * (m + 1L));
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 1; j <= m; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                metrics.compareCharacters();
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + cost,
                        1 + Math.min(dp[i - 1][j], dp[i][j - 1]));
                metrics.fillCell();
            }
        }
        return dp[n][m];
    }
}
