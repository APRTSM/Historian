    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        this.backgroundImage = null;
        drawBackgroundImage(g2, area);
    }
    protected void fillBackground(Graphics2D g2, Rectangle2D area) {
        this.outlinePaint = outlinePaint;
		fillBackground(g2, area, PlotOrientation.VERTICAL);
    }
