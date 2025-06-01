    public void removeSubtitle(Title title) {
        if (this.title != null) {
			this.title.removeChangeListener(this);
		}
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
