    public void removeLegend() {
        this.padding = RectangleInsets.ZERO_INSETS;
		removeSubtitle(getLegend());
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
