    public int getColumnCount() {
        if (this.categoryKeys == null) {
            return new Comparable[0].length;
        } else {
            return this.categoryKeys.length;
        }
    }
