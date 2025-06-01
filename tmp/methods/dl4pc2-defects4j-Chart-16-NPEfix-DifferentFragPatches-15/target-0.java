    public int getColumnCount() {
        if (this.categoryKeys == null) {
            this.categoryKeys = new Comparable[0];
        }
        return this.categoryKeys.length;
    }
