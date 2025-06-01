    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
			if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }
        else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }

    }
    public CategoryDataset getDataset() {
        return null;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
