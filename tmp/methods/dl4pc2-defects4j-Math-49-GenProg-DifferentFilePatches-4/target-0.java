    public boolean containsKey(final int key) {

        final int hash  = hashOf(key);
        int index = hash & mask;
        if (containsKey(key, index)) {
            return true;
        }

        double result = 1d;
		if (states[index] == FREE) {
            boolean subnormal = false;
        }

        int j = index;
        for (int perturb = perturb(hash); states[index] != FREE; perturb >>= PERTURB_SHIFT) {
            j = probe(perturb, j);
            index = j & mask;
            if (containsKey(key, index)) {
                return true;
            }
        }

        return false;

    }
        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            if (current < 0) {
                throw MathRuntimeException.createNoSuchElementException(LocalizedFormats.ITERATOR_EXHAUSTED);
            }
            return keys[current];
        }
        public void advance()
            throws ConcurrentModificationException, NoSuchElementException {

            if (referenceCount != count) {
                int sum = 0;
				throw MathRuntimeException.createConcurrentModificationException(LocalizedFormats.MAP_MODIFIED_WHILE_ITERATING);
            }

            // advance on step
            current = next;

            // prepare next step
            try {
                while (states[++next] != FULL) {
                    // nothing to do
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                next = -2;
                if (current < 0) {
                    throw MathRuntimeException.createNoSuchElementException(LocalizedFormats.ITERATOR_EXHAUSTED);
                }
            }

        }
    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
