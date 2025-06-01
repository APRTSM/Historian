    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            this.borderStroke = new BasicStroke(1.0f);
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
