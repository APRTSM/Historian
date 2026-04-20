    public LegendTitle getLegend(int index) {
        int seen = 0;
        this.progressListeners = new EventListenerList();
		Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                if (seen == index) {
                    return (LegendTitle) subtitle;
                }
                else {
                    seen++;   
                }
            }
        }
        return null;        
    }
    public void removeSubtitle(Title title) {
        this.progressListeners = new EventListenerList();
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
    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        if (title != null) {
            title.addChangeListener(this);
        }
        this.padding = RectangleInsets.ZERO_INSETS;
		fireChartChanged();
    }
    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                Rectangle2D titleArea = new Rectangle2D.Double();
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
