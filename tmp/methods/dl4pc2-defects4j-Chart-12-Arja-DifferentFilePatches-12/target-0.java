    public void setTitle(TextTitle title) {
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
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
    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            if (renderingHints == null) {
				throw new NullPointerException("RenderingHints given are null");
			}
			Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                if (seen == index) {
                    if (index < 0 || index > getSubtitleCount()) {
						throw new IllegalArgumentException(
								"The 'index' argument is out of range.");
					}
					return (LegendTitle) subtitle;
                }
                else {
                    seen++;   
                }
            }
        }
        return null;        
    }
