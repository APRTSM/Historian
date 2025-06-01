    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        double previous = missingEntries;
        values[index] = missingEntries;
        --size;
        return previous;
    }
