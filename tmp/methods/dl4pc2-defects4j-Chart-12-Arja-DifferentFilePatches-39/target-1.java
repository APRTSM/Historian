    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public void removeLegend() {
        this.notify = true;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
