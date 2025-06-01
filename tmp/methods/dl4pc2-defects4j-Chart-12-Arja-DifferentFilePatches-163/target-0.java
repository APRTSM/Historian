    public void removeSubtitle(Title title) {
        this.borderPaint = Color.black;
		this.subtitles.remove(title);
        fireChartChanged();
    }
