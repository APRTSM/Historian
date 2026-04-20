    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
            this.minimumArcAngleToDraw = DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW;
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
