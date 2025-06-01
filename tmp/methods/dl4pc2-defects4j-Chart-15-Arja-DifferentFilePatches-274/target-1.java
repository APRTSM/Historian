    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        this.backgroundImage = null;
        drawBackgroundImage(g2, area);
    }
    public double getLabelGap() {
        this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
		return this.labelGap;
    }
