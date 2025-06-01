    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.changeListeners = new EventListenerList();
		fireChartChanged();
    }
    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
