    public void fireChartChanged() {
        this.notify = true;
		ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }
