    public void setTitle(TextTitle title) {
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        return true;
    }
