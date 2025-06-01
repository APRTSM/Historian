    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.borderVisible = false;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
