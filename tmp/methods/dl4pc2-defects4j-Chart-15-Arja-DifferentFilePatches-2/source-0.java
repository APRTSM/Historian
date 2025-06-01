    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        // some subclasses override this method completely, so don't put 
        // anything here that *must* be done
        fillBackground(g2, area);
        drawBackgroundImage(g2, area);
    }
    protected void fillBackground(Graphics2D g2, Rectangle2D area) {
        fillBackground(g2, area, PlotOrientation.VERTICAL);
    }
