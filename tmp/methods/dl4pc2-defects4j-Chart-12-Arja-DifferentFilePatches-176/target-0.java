    public void setTitle(TextTitle title) {
        this.notify = true;
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
