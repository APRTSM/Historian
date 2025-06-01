    public void removeSubtitle(Title title) {
        if (this.title != null) {
			this.title.removeChangeListener(this);
		}
		this.subtitles.remove(title);
        fireChartChanged();
    }
    protected void notifyListeners(ChartChangeEvent event) {
        if (this.notify) {
            Object[] listeners = this.changeListeners.getListenerList();
            this.subtitles.remove(title);
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChartChangeListener.class) {
                    ((ChartChangeListener) listeners[i + 1]).chartChanged(
                            event);
                }
            }
        }
    }
    public boolean hasListener(EventListener listener) {
        this.listenerList = new EventListenerList();
		List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
