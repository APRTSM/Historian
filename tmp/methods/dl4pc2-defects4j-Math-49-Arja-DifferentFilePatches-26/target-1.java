    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public void setEntry(int index, double value) {
        checkIndex(index);
        if (!isDefaultValue(value)) {
            if (!isDefaultValue(value)) {
				entries.put(index, value);
			} else if (entries.containsKey(index)) {
				entries.remove(index);
			}
        } else if (entries.containsKey(index)) {
            entries.remove(index);
        }
    }
