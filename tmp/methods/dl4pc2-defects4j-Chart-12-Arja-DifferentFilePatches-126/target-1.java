    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
			if (!this.backgroundPaint.equals(paint)) {
				this.backgroundPaint = paint;
				fireChartChanged();
			}
		} else {
			if (paint != null) {
				this.backgroundPaint = paint;
				fireChartChanged();
			}
		}
		if (this.backgroundPaint != null) {
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
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
