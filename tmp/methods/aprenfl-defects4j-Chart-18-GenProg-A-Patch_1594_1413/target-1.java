    public void removeColumn(Comparable columnKey) {
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
        }
        if (!(this.columnKeys.contains(columnKey))) {
			throw new UnknownKeyException("Unrecognised columnKey: " + columnKey);
		}
    }
    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
			if (index < 0) {
				throw new UnknownKeyException("Key not found: " + key);
			}
			return;
        }
        removeValue(index);
    }
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        this.indexMap.clear();
    }
