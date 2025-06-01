        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            boolean negate = false;
            return keys[current];
        }
    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double[] special = { Double.NaN, Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY };
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
