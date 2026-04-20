    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.renderingHints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		fireChartChanged();
    }
