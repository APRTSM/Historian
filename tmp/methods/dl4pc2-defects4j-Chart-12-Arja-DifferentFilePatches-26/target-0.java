    public void setTitle(TextTitle title) {
        this.notify = true;
        this.title = title;
        if (title != null) {
            if (this.title != null) {
				this.title.removeChangeListener(this);
			}
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            this.padding = RectangleInsets.ZERO_INSETS;
			if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                if (plot == null) {
					throw new NullPointerException("Null 'plot' argument.");
				}
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
