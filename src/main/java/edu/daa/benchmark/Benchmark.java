package edu.daa.benchmark;

import edu.daa.edit.EditDistance;
import edu.daa.edit.EditMetrics;
import edu.daa.edit.RecursiveEditDistance;
import edu.daa.edit.TableEditDistance;
import edu.daa.edit.TwoRowEditDistance;
import edu.daa.search.KmpSearch;
import edu.daa.search.NaiveSearch;
import edu.daa.search.SearchMetrics;
import edu.daa.search.StringSearch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class Benchmark {
    private static final int WARMUPS = 3;
    private static final int TRIALS = 5;
    private static volatile long consumed;

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("results", "results.csv") : Path.of(args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("algorithm,input,n,m,seed,trial,batch,time_ms,distance,match_count,"
                    + "recursive_calls,edit_comparisons,dp_cells,buffer_ints,search_comparisons,lps_comparisons");
            writer.newLine();
            runEdit(writer, "recursive", new RecursiveEditDistance(), new int[]{2, 4, 6, 8});
            runEdit(writer, "table", new TableEditDistance(), new int[]{128, 256, 512, 1024});
            runEdit(writer, "two_rows", new TwoRowEditDistance(), new int[]{128, 256, 512, 1024});
            runSearch(writer, "naive", new NaiveSearch());
            runSearch(writer, "kmp", new KmpSearch());
        }
        System.out.println("Wrote 200 measured trials to " + output);
    }

    private static void runEdit(BufferedWriter writer, String name, EditDistance algorithm, int[] sizes)
            throws IOException {
        for (int n : sizes) {
            for (boolean disjoint : new boolean[]{false, true}) {
                ExperimentInputs.Pair pair = ExperimentInputs.edit(n, disjoint);
                String input = disjoint ? "disjoint" : "identical";
                int batch = editBatch(name, n, disjoint);
                int expected = disjoint ? n : 0;
                for (int trial = 1 - WARMUPS; trial <= TRIALS; trial++) {
                    EditMetrics metrics = new EditMetrics();
                    long totalDistance = 0;
                    long start = System.nanoTime();
                    for (int call = 0; call < batch; call++) {
                        totalDistance += algorithm.distance(pair.first(), pair.second(), metrics);
                    }
                    double timeMs = millisecondsPerCall(start, batch);
                    consumed = totalDistance;
                    if (totalDistance != (long) expected * batch) {
                        throw new IllegalStateException("Unexpected edit distance for " + input);
                    }
                    if (trial > 0) {
                        write(writer, name, input, n, n, trial, batch, timeMs, Integer.toString(expected), "",
                                perCall(metrics.recursiveCalls(), batch),
                                perCall(metrics.characterComparisons(), batch),
                                perCall(metrics.dpCells(), batch),
                                perCall(metrics.bufferInts(), batch), 0, 0);
                    }
                }
            }
        }
    }

    private static int editBatch(String algorithm, int n, boolean disjoint) {
        if (!algorithm.equals("recursive")) {
            return Math.max(1, 262_144 / (n * n));
        }
        if (!disjoint) {
            return 8_192;
        }
        return switch (n) {
            case 2 -> 4_096;
            case 4 -> 256;
            case 6 -> 16;
            default -> 2;
        };
    }

    private static void runSearch(BufferedWriter writer, String name, StringSearch algorithm) throws IOException {
        for (int n : new int[]{4_096, 8_192, 16_384, 32_768}) {
            int m = n / 64;
            for (boolean lateMismatch : new boolean[]{false, true}) {
                ExperimentInputs.Pair pair = ExperimentInputs.search(n, m, lateMismatch);
                String input = lateMismatch ? "late_mismatch" : "random_planted";
                int batch = lateMismatch && name.equals("naive")
                        ? Math.max(1, 1_048_576 / (n * m))
                        : 65_536 / n;
                List<Integer> expected = lateMismatch ? List.of() : List.of(n / 3);
                for (int trial = 1 - WARMUPS; trial <= TRIALS; trial++) {
                    SearchMetrics metrics = new SearchMetrics();
                    List<Integer> matches = List.of();
                    long checksum = 0;
                    long start = System.nanoTime();
                    for (int call = 0; call < batch; call++) {
                        matches = algorithm.search(pair.first(), pair.second(), metrics);
                        checksum += matches.hashCode();
                    }
                    double timeMs = millisecondsPerCall(start, batch);
                    consumed = checksum;
                    if (!matches.equals(expected)) {
                        throw new IllegalStateException("Unexpected match list for " + input + ": " + matches);
                    }
                    if (trial > 0) {
                        write(writer, name, input, n, m, trial, batch, timeMs, "", Integer.toString(matches.size()),
                                0, 0, 0, 0,
                                perCall(metrics.searchComparisons(), batch), perCall(metrics.lpsComparisons(), batch));
                    }
                }
            }
        }
    }

    private static double millisecondsPerCall(long start, int batch) {
        return (System.nanoTime() - start) / 1_000_000.0 / batch;
    }

    private static long perCall(long count, int batch) {
        if (count % batch != 0) {
            throw new IllegalStateException("Counter varied across identical calls");
        }
        return count / batch;
    }

    private static void write(BufferedWriter writer, String algorithm, String input, int n, int m, int trial,
                              int batch, double timeMs, String distance, String matches, long calls, long editCmp,
                              long cells, long slots, long searchCmp, long lpsCmp) throws IOException {
        writer.write(String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%d,%d,%.9f,%s,%s,%d,%d,%d,%d,%d,%d",
                algorithm, input, n, m, ExperimentInputs.SEED, trial, batch, timeMs, distance, matches,
                calls, editCmp, cells, slots, searchCmp, lpsCmp));
        writer.newLine();
    }
}
