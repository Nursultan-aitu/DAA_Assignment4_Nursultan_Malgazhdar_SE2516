package edu.daa.edit;

public interface EditDistance {
    default int distance(String a, String b) {
        return distance(a, b, new EditMetrics());
    }

    int distance(String a, String b, EditMetrics metrics);
}
