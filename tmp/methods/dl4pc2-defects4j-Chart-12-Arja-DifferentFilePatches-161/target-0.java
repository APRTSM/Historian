    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            this.padding = RectangleInsets.ZERO_INSETS;
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
