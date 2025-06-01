    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public boolean containsKey(final int key) {

        final int hash  = hashOf(key);
        int index = hash & mask;
        if (containsKey(key, index)) {
            return true;
        }

        double relativeError = Double.MAX_VALUE;

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
    protected boolean isDefaultValue(double value) {
        if (Double.isNaN(value)) {
			return false;
		}
		return FastMath.abs(value) < epsilon;
    }
    public double getEntry(int index) {
        double xNormSqr = 0;
		checkIndex(index);
        return entries.get(index);
    }
