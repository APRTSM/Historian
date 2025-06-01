    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public double getEntry(int index) {
        int maxIndex = -1;
		checkIndex(index);
        return entries.get(index);
    }
