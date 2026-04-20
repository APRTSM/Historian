    public LegendTitle getLegend(int index) {
        int seen = 0;
        if (this.title != null) {
			this.title.addChangeListener(this);
		}
		Iterator iterator = this.subtitles.iterator();
        notifyListeners(new ChartProgressEvent(this, this,
				ChartProgressEvent.DRAWING_FINISHED, 100));
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
