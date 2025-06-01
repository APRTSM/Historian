    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        this.notify = true;
		if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
