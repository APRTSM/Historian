    public void removeColumn(Comparable columnKey) {
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
        }
        if (!(this.columnKeys.contains(columnKey))) {
			throw new UnknownKeyException("Unrecognised columnKey: " + columnKey);
		}
    }
