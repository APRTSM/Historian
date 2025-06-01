    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        this.title.addChangeListener(this);
        fireChartChanged();
    }
