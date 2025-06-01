    public void removeLegend() {
        this.padding = padding;
		removeSubtitle(getLegend());
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
