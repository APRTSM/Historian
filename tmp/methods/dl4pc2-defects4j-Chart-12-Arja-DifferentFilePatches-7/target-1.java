    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
    }
    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        notifyListeners(new ChartProgressEvent(this, this,
				ChartProgressEvent.DRAWING_FINISHED, 100));
		while (iterator.hasNext()) {
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
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
