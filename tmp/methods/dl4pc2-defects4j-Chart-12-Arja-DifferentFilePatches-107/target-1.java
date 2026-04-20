    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void fireChartChanged() {
        this.notify = true;
		ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
