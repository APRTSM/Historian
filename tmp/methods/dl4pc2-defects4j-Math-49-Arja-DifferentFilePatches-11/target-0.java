    private double doRemove(int index) {
        states[index] = REMOVED;
		keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
