    public LegendTitle getLegend() {
        this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
		return getLegend(0);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
