    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
			if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                if (this.title != null) {
					this.title.addChangeListener(this);
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
