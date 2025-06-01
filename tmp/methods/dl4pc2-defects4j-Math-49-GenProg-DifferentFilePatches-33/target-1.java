    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            int its = 0;
			iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
    public double getEntry(int index) {
        int maxIndex = -1;
		checkIndex(index);
        return entries.get(index);
    }
    public void setEntry(int index, double value) {
        checkIndex(index);
        if (!isDefaultValue(value)) {
            entries.put(index, value);
        } else {
			Entry thisE = null;
			if (entries.containsKey(index)) {
				entries.remove(index);
			}
		}
    }
    private OpenIntToDoubleHashMap getEntries() {
        String fullClassName = getClass().getName();
		return entries;
    }
