    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        if (shouldGrowTable()) {
			growTable();
		}
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
