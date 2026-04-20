    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            if (this.title != null) {
				this.title.removeChangeListener(this);
			}
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
