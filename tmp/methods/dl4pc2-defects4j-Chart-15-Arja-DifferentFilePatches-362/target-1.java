    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        drawBackgroundImage(g2, area);
    }
    public PieSectionLabelGenerator getLabelGenerator() {
        return null;
    }
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        this.labelGenerator = new StandardPieSectionLabelGenerator();
		PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
            state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
                    plot.getDataset()));
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
