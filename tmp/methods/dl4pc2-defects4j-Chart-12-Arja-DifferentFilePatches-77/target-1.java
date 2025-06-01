    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
