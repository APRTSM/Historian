    public void setTitle(TextTitle title) {
        plot.addChangeListener(this);
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
