    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
		fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
