    protected void fillBackground(Graphics2D g2, Rectangle2D area) {
    }
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        this.simpleLabels = false;
		PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
            this.sectionOutlineStrokeMap = new StrokeMap();
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
