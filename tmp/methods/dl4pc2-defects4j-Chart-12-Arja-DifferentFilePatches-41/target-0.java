    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.title = title;
		notifyListeners(event);
    }
