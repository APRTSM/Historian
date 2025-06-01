    public void removeSubtitle(Title title) {
        this.renderingHints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
        fireChartChanged();
    }
