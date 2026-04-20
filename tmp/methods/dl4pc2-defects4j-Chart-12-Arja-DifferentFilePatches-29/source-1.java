    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public void setBackgroundPaint(Paint paint) {

        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }
        else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }

    }
    public CategoryDataset getDataset() {
        return this.dataset;
    }
