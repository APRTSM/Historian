    public void setTitle(TextTitle title) {
        if (this.title != null) {
			this.title.addChangeListener(this);
		}
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void removeSubtitle(Title title) {
        this.progressListeners = new EventListenerList();
        fireChartChanged();
    }
