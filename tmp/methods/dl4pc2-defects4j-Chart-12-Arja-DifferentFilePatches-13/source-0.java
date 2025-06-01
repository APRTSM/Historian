    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        fireChartChanged();
    }
    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }
