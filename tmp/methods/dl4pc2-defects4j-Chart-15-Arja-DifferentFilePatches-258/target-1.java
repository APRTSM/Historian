    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        this.backgroundImage = null;
        drawBackgroundImage(g2, area);
    }
    public double getInteriorGap() {
        this.ignoreZeroValues = false;
		return this.interiorGap;
    }
