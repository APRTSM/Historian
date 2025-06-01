    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.padding = RectangleInsets.ZERO_INSETS;
		notifyListeners(event);
    }
