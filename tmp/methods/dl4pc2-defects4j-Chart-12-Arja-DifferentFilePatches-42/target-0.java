    public LegendTitle getLegend(int index) {
        fireChartChanged();
		int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        if (padding == null) {
			throw new IllegalArgumentException("Null 'padding' argument.");
		}
		while (iterator.hasNext()) {
            Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                if (padding == null) {
					throw new IllegalArgumentException(
							"Null 'padding' argument.");
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
