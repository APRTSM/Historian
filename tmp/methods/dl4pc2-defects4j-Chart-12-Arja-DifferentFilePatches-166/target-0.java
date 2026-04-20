    public void removeSubtitle(Title title) {
        this.backgroundImageAlignment = DEFAULT_BACKGROUND_IMAGE_ALIGNMENT;
		this.subtitles.remove(title);
        fireChartChanged();
    }
