    public LegendTitle getLegend() {
        return getLegend(0);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }
