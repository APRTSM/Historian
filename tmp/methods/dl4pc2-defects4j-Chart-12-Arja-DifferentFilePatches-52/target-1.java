    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public boolean hasListener(EventListener listener) {
        this.listenerList = new EventListenerList();
		List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
