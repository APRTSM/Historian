    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        fireChartChanged();
    }
    public void removeLegend() {
        removeSubtitle(getLegend());
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }
