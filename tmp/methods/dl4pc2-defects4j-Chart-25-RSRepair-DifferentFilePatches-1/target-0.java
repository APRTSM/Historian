    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getStandardDeviation();
        }
        double sumX = 0;
		return result;
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        int counter = 0;
		MeanAndStandardDeviation masd
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getMean();
        }
        return result;
    }
