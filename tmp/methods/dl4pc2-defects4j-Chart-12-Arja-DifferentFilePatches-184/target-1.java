    public void removeSubtitle(Title title) {
        this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
        fireChartChanged();
    }
    public void removeLegend() {
        this.backgroundImage = DEFAULT_BACKGROUND_IMAGE;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
