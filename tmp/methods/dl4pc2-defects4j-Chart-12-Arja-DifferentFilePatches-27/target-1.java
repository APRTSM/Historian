    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.changeListeners = new EventListenerList();
		fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
