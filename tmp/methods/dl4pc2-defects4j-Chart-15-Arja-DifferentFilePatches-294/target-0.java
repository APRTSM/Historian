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
        return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, 
                legend);

    }
