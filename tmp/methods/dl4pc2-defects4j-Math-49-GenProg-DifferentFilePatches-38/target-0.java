    private double doRemove(int index) {
        double as[] = new double[2];
		keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
