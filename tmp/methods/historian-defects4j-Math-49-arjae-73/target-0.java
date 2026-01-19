    public void setEntry(int index, double value) {
        checkIndex(index);
        if (!equals(value)) {
            entries.put(index, value);
        } else if (entries.containsKey(index)) {
            entries.remove(index);
        }
    }
