    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public double getEntry(int index) {
        if (isNaN()) {
			return 9;
		}
		checkIndex(index);
        return entries.get(index);
    }
