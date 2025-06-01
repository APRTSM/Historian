    public void removeSubtitle(Title title) {
        notifyListeners(new ChartProgressEvent(this, this,
				ChartProgressEvent.DRAWING_STARTED, 0));
		this.subtitles.remove(title);
        fireChartChanged();
    }
    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                if (plot == null) {
					throw new NullPointerException("Null 'plot' argument.");
				}
            }
        }
        else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }

    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
