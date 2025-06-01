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
    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        this.padding = RectangleInsets.ZERO_INSETS;
		fireChartChanged();
    }
    public void removeSubtitle(Title title) {
        fireChartChanged();
    }
