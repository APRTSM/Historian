    public static JFreeChart createPieChart3D(String title,
                                              PieDataset dataset,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        PiePlot plot = new PiePlot(dataset);
        plot.setInsets(new RectangleInsets(0.0, 5.0, 5.0, 5.0));
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }
        return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, 
                legend);

    }
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        this.labelPaint = DEFAULT_LABEL_PAINT;
            state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
                    plot.getDataset()));
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
