    public void setTitle(TextTitle title) {
        if (padding == null) {
			throw new IllegalArgumentException("Null 'padding' argument.");
		}
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
