    public void removeLegend() {
        this.padding = RectangleInsets.ZERO_INSETS;
    }
    public boolean hasListener(EventListener listener) {
        this.listenerList = new EventListenerList();
		List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
