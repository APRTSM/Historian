    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.changeListeners = new EventListenerList();
		fireChartChanged();
    }
