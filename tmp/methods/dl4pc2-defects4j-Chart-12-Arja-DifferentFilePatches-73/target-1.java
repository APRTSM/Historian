    public void removeLegend() {
        this.notify = true;
		removeSubtitle(getLegend());
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
