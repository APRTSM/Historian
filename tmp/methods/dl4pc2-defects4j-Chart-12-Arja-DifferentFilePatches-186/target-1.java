    public void removeSubtitle(Title title) {
        this.progressListeners = new EventListenerList();
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
