    public int getSeriesIndex(Comparable seriesKey) {
        int result = -1;
        if (this.seriesKeys == null) {
            this.seriesKeys = new Comparable[0];
        }
        for (int i = 0; i < this.seriesKeys.length; i++) {
            if (seriesKey.equals(this.seriesKeys[i])) {
                result = i;
                break;
            }
        }
        return result;
    }
