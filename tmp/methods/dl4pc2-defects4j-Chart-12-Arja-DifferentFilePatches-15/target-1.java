    public LegendTitle getLegend() {
        this.plot = plot;
		return getLegend(0);
    }
    public void setTitle(TextTitle title) {
        this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
