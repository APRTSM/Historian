    public void setTitle(TextTitle title) {
        this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
