    public static JFreeChart createPieChart3D(String title,
                                              PieDataset dataset,
                                              boolean legend,
                                              boolean tooltips,
                                              boolean urls) {

        PiePlot3D plot = new PiePlot3D(dataset);
        plot.setURLGenerator(new StandardPieURLGenerator());
        if (tooltips) {
            plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        if (urls) {
            plot.setURLGenerator(new StandardPieURLGenerator());
        }
        return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, 
                legend);

    }
    public double getLabelGap() {
        this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
		return this.labelGap;
    }
