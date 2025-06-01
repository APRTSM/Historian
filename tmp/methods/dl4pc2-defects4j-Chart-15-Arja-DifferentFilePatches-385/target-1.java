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
    protected void fillBackground(Graphics2D g2, Rectangle2D area) {
    }
