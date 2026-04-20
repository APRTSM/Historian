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

        StatisticalCategoryDataset statData = (StatisticalCategoryDataset) data;

        PlotOrientation orientation = plot.getOrientation();
    }
    public int getColumnCount() {
        return 1;
    }
