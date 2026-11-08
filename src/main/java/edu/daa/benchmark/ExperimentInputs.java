package edu.daa.benchmark;

import java.util.Random;

final class ExperimentInputs {
    static final long SEED = 42;

    private ExperimentInputs() {
    }

    record Pair(String first, String second) {
    }

    static Pair edit(int length, boolean disjoint) {
        Random random = new Random(SEED + length);
        String a = generate(random, "ABCD", length);
        String b = disjoint ? generate(random, "WXYZ", length) : a;
        return new Pair(a, b);
    }

    static Pair search(int textLength, int patternLength, boolean lateMismatch) {
        if (lateMismatch) {
            return new Pair("A".repeat(textLength), "A".repeat(patternLength - 1) + "B");
        }
        Random random = new Random(SEED + textLength);
        String text = generate(random, "ABCD", textLength);
        String pattern = generate(random, "ABCD", patternLength);
        int start = textLength / 3;
        return new Pair(text.substring(0, start) + pattern + text.substring(start + patternLength), pattern);
    }

    private static String generate(Random random, String alphabet, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return result.toString();
    }
}
