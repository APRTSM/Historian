    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            Title subtitle = (Title) iterator.next();
            this.renderingHints = renderingHints;
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
    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
