    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
            setCircular(false, false);
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
    public double getMaximumLabelWidth() {
        this.legendLabelURLGenerator = null;
		return this.maximumLabelWidth;
    }
