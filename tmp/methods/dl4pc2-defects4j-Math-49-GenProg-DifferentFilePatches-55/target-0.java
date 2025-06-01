    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        keys[index] = 0;
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            if (referenceCount != count) {
                throw MathRuntimeException.createConcurrentModificationException(LocalizedFormats.MAP_MODIFIED_WHILE_ITERATING);
            }
            boolean negate = false;
            return keys[current];
        }
