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
    public void removeSubtitle(Title title) {
        this.progressListeners = new EventListenerList();
		fireChartChanged();
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
    public void setTitle(TextTitle title) {
        Object[] listeners = this.changeListeners.getListenerList();
		if (title != null) {
            title.addChangeListener(this);
        }
        this.padding = RectangleInsets.ZERO_INSETS;
		fireChartChanged();
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof LegendTitle)) {
            return false;   
        }
        if (!super.equals(obj)) {
            return false;   
        }
        LegendTitle that = (LegendTitle) obj;
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;   
        }
        if (this.legendItemGraphicEdge != that.legendItemGraphicEdge) {
            return false;   
        }
        if (this.legendItemGraphicAnchor != that.legendItemGraphicAnchor) {
            return false;   
        }
        if (this.legendItemGraphicLocation != that.legendItemGraphicLocation) {
            return false;   
        }
        if (!this.itemFont.equals(that.itemFont)) {
            return false;   
        }
        if (!this.itemPaint.equals(that.itemPaint)) {
            return false;   
        }
        if (!this.hLayout.equals(that.hLayout)) {
            return false;   
        }
        if (!this.vLayout.equals(that.vLayout)) {
            return false;   
        }
        return true;
    }
