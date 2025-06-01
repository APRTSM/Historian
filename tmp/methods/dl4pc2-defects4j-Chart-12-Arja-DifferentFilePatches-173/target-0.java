    public void setTitle(TextTitle title) {
        this.title = title;
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
