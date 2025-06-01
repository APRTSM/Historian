    public void removeSubtitle(Title title) {
        if (this.title != null) {
			this.title.removeChangeListener(this);
		}
        fireChartChanged();
    }
