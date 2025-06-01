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
        if (!(data instanceof StatisticalCategoryDataset)) {
			throw new IllegalArgumentException(
					"Requires StatisticalCategoryDataset.");
		}
    }
    public double getLowerClip() {
        this.base = base;
		// TODO:  this attribute should be transferred to the renderer state.
        return this.lowerClip;
    }
