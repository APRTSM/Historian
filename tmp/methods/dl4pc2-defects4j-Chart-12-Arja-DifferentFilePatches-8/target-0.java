    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            this.title = title;
			Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                for (int i = 0; i < getSubtitleCount(); i++) {
					getSubtitle(i).addChangeListener(this);
				}
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

        this.borderStroke = new BasicStroke(1.0f);
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
