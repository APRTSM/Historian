    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.borderPaint = Color.black;
		fireChartChanged();
    }
