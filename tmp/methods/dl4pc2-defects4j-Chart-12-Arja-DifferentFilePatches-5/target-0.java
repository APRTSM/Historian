    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        this.title = title;
		notifyListeners(event);
    }
    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                if (subtitle instanceof LegendTitle) {
					if (seen == index) {
						return (LegendTitle) subtitle;
					} else {
						seen++;
					}
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
