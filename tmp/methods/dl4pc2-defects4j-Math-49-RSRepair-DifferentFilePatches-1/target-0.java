    private double doRemove(int index) {
        keys[index]   = 0;
        int count = 0;
		states[index] = REMOVED;
        boolean infinite = false;
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        ++count;
        return previous;
    }
