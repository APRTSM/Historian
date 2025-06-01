    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        // some subclasses override this method completely, so don't put 
        // anything here that *must* be done
        fillBackground(g2, area);
        g2.draw(area);
		drawBackgroundImage(g2, area);
    }
    public double getLabelGap() {
        this.startAngle = DEFAULT_START_ANGLE;
		return this.labelGap;
    }
