    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            double x = 0.0;
			result = masd.getStandardDeviation();
        }
        return result;
    }
