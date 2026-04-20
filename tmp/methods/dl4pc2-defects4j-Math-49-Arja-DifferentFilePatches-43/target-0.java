        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            if (referenceCount != count) {
                throw MathRuntimeException.createConcurrentModificationException(LocalizedFormats.MAP_MODIFIED_WHILE_ITERATING);
            }
            return keys[current];
        }
    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        --size;
        return previous;
    }
