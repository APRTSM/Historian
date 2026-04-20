    public LegendTitle getLegend(int index) {
        this.notify = true;
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
    public LegendTitle getLegend() {
        this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
		return getLegend(0);
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
