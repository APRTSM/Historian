    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                this.plot.addChangeListener(this);
            }
        }
        else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }

    }
