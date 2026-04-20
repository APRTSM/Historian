    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.title = title;
		notifyListeners(event);
    }
