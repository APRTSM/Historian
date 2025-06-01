    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.title = title;
		notifyListeners(event);
    }
    public boolean hasListener(EventListener listener) {
        this.listenerList = new EventListenerList();
		List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
