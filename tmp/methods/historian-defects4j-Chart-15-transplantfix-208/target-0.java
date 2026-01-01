    public void draw(Graphics2D g2, Rectangle2D plotArea, Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo info) {

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(plotArea);

        Rectangle2D originalPlotArea = (Rectangle2D) plotArea.clone();
        if (info != null) {
            info.setPlotArea(plotArea);
            info.setDataArea(plotArea);
        }

        drawBackground(g2, plotArea);

        
        drawOutline(g2, originalPlotArea);

    }
