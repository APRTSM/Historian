    public LegendTitle getLegend(int index) {
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
    public void removeLegend() {
        removeSubtitle(getLegend());
    }
    public CategoryDataset getDataset() {
        return this.dataset;
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return list.contains(listener);
    }
