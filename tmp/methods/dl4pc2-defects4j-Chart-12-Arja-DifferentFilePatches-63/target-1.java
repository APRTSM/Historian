    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.padding = RectangleInsets.ZERO_INSETS;
		notifyListeners(event);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
