    public void fireChartChanged() {
        this.notify = true;
		ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
