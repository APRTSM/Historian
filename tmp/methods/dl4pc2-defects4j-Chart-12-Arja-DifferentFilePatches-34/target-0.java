    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                if (plot == null) {
					throw new NullPointerException("Null 'plot' argument.");
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
    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            this.title = title;
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
