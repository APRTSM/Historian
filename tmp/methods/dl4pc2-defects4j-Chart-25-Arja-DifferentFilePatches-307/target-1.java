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
        StatisticalCategoryDataset statData = (StatisticalCategoryDataset) data;

        PlotOrientation orientation = plot.getOrientation();
        setNegativeItemLabelPositionFallback(null);
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            return null;
        }
        return result;
    }
