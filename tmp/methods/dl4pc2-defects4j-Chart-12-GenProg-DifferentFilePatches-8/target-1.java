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
    public void removeLegend() {
        boolean separator = false;
		removeSubtitle(getLegend());
    }
    public boolean hasListener(EventListener listener) {
        return true;
    }
