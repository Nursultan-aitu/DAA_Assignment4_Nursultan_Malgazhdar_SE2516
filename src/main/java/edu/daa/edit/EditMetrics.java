package edu.daa.edit;

/** Counts work across calls that share this instance. */
public final class EditMetrics {
    private long recursiveCalls;
    private long characterComparisons;
    private long dpCells;
    private long bufferInts;

    void visitRecursiveCall() {
        recursiveCalls++;
    }

    void compareCharacters() {
        characterComparisons++;
    }

    void fillCell() {
        dpCells++;
    }

    void allocateBuffer(long slots) {
        bufferInts += slots;
    }

    public long recursiveCalls() {
        return recursiveCalls;
    }

    public long characterComparisons() {
        return characterComparisons;
    }

    public long dpCells() {
        return dpCells;
    }

    public long bufferInts() {
        return bufferInts;
    }
}
