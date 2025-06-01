    private double doRemove(int index) {
        keys[index]   = 0;
        if (shouldGrowTable()) {
			growTable();
		}
		states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
