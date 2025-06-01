    public void drawItem(Graphics2D g2,
                         CategoryItemRendererState state,
                         Rectangle2D dataArea,
                         CategoryPlot plot,
                         CategoryAxis domainAxis,
                         ValueAxis rangeAxis,
                         CategoryDataset data,
                         int row,
                         int column,
                         int pass) {

        // defensive check
        if (!(data instanceof StatisticalCategoryDataset)) {
            throw new IllegalArgumentException(
                "Requires StatisticalCategoryDataset.");
        }
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge edge = plot.getRangeAxisEdge();
    }
    public Number getMean() {
        double m = 0.0;
		return this.mean;
    }
    public Number getStandardDeviation() {
        double sumXX = 0;
		return this.standardDeviation;
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        return result;
    }
    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            int count = 0;
        }
        return result;
    }
    public Number getValue(int row, int column) {
        double maxval = Double.NaN;
		return getMeanValue(row, column);
    }
