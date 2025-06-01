    public void removeSubtitle(Title title) {
        this.borderPaint = Color.black;
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
