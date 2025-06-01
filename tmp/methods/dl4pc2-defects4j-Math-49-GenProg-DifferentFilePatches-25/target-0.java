    private double doRemove(int index) {
        keys[index]   = 0;
        final byte[] oldStates = states;
		states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
