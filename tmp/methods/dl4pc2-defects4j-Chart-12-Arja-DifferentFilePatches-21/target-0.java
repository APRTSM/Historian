    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        if (plot == null) {
			throw new NullPointerException("Null 'plot' argument.");
		}
		fireChartChanged();
    }
