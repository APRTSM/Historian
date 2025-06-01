    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        this.borderPaint = Color.black;
		fireChartChanged();
    }
    public LegendTitle getLegend(int index) {
        this.padding = RectangleInsets.ZERO_INSETS;
		int seen = 0;
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
