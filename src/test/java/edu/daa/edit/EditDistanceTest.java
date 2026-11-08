package edu.daa.edit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditDistanceTest {
    private final EditDistance recursive = new RecursiveEditDistance();
    private final EditDistance table = new TableEditDistance();
    private final EditDistance twoRows = new TwoRowEditDistance();
    private final List<EditDistance> versions = List.of(recursive, table, twoRows);

    @Test
    void matchesReferenceDistances() {
        check(3, "kitten", "sitting");
        check(2, "flaw", "lawn");
        check(5, "intention", "execution");
        check(0, "abc", "abc");
    }

    @Test
    void handlesEmptyStringsInEitherOrder() {
        check(3, "", "abc");
        check(3, "abc", "");
        check(0, "", "");
    }

    @Test
    void agreesOnOneHundredSeededShortPairs() {
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            String a = word(random);
            String b = word(random);
            check(table.distance(a, b), a, b);
        }
    }

    @Test
    void countsRecursiveVisitsIncludingBaseCases() {
        EditMetrics metrics = new EditMetrics();
        assertEquals(1, recursive.distance("a", "b", metrics));
        assertEquals(4, metrics.recursiveCalls());
        assertEquals(1, metrics.characterComparisons());
    }

    @Test
    void dpVersionsFillTheSameCellsButAllocateDifferentBuffers() {
        EditMetrics full = new EditMetrics();
        EditMetrics compact = new EditMetrics();
        assertEquals(table.distance("ab", "xyz", full), twoRows.distance("ab", "xyz", compact));
        assertEquals(6, full.dpCells());
        assertEquals(6, compact.dpCells());
        assertEquals(12, full.bufferInts());
        assertEquals(8, compact.bufferInts());
    }

    private void check(int expected, String a, String b) {
        for (EditDistance algorithm : versions) {
            assertEquals(expected, algorithm.distance(a, b),
                    () -> algorithm.getClass().getSimpleName() + " on [" + a + ", " + b + "]");
        }
    }

    private static String word(Random random) {
        int length = random.nextInt(7);
        StringBuilder word = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            word.append((char) ('a' + random.nextInt(4)));
        }
        return word.toString();
    }
}
