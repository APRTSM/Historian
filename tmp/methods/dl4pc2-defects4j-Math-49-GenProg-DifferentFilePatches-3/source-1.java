        public void advance()
            throws ConcurrentModificationException, NoSuchElementException {

            if (referenceCount != count) {
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
        ++count;
        return previous;
    }
        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            if (referenceCount != count) {
                throw MathRuntimeException.createConcurrentModificationException(LocalizedFormats.MAP_MODIFIED_WHILE_ITERATING);
            }
            if (current < 0) {
                throw MathRuntimeException.createNoSuchElementException(LocalizedFormats.ITERATOR_EXHAUSTED);
            }
            return keys[current];
        }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
    public void setEntry(int index, double value) {
        checkIndex(index);
        if (!isDefaultValue(value)) {
            entries.put(index, value);
        } else if (entries.containsKey(index)) {
            entries.remove(index);
        }
    }
