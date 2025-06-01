    public void removeLegend() {
        removeSubtitle(getLegend());
    }
    public CategoryDataset getDataset() {
        return this.dataset;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }
