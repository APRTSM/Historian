    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.plot = plot;
		fireChartChanged();
    }
