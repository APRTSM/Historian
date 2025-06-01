    public void removeSubtitle(Title title) {
        this.progressListeners = new EventListenerList();
		fireChartChanged();
    }
    public void setTitle(TextTitle title) {
        Object[] listeners = this.changeListeners.getListenerList();
		if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        if (title != null) {
            title.addChangeListener(this);
        }
        this.padding = RectangleInsets.ZERO_INSETS;
		fireChartChanged();
    }
    protected void notifyListeners(ChartChangeEvent event) {
        if (this.notify) {
            if (padding == null) {
				throw new IllegalArgumentException("Null 'padding' argument.");
			}
			Object[] listeners = this.changeListeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChartChangeListener.class) {
                    ((ChartChangeListener) listeners[i + 1]).chartChanged(
                            event);
                }
            }
        }
    }
    public void setBackgroundPaint(Paint paint) {

        this.title = title;
		if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                Object retValue = null;
            }
        }
        else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }

    }
