    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        if (plot == null) {
			throw new NullPointerException("Null 'plot' argument.");
		}
		fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
