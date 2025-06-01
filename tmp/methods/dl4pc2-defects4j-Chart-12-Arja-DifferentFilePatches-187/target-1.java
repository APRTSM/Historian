    public void removeSubtitle(Title title) {
        this.subtitles = new ArrayList();
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
