    public LegendTitle getLegend(int index) {
        int seen = 0;
        if (index < 0 || index > getSubtitleCount()) {
			throw new IllegalArgumentException(
					"The 'index' argument is out of range.");
		}
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
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
