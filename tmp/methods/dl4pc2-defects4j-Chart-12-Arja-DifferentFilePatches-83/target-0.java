    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.renderingHints = renderingHints;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public void fireChartChanged() {
        this.notify = true;
		ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }
