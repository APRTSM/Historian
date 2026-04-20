    public void removeSubtitle(Title title) {
        this.backgroundImageAlignment = DEFAULT_BACKGROUND_IMAGE_ALIGNMENT;
        fireChartChanged();
    }
    public void removeLegend() {
        this.padding = padding;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
