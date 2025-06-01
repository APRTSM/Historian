    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
		fireChartChanged();
    }
