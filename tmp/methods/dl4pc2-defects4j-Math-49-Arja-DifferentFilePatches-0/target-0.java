    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        if (states[index] == FULL) {
			return changeIndexSign(index);
		}
        --size;
        return previous;
    }
        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            if (referenceCount != count) {
                throw MathRuntimeException.createConcurrentModificationException(LocalizedFormats.MAP_MODIFIED_WHILE_ITERATING);
            }
            return keys[current];
        }
