    public boolean hasListener(EventListener listener) {
        notifyListeners(new DatasetChangeEvent(this, this));
		List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
