    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            for (int i = 0; i < getSubtitleCount(); i++) {
				getSubtitle(i).addChangeListener(this);
			}
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
