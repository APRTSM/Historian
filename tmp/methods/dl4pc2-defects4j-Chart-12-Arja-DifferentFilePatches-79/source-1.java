    public LegendTitle getLegend(int index) {
        int seen = 0;
        Iterator iterator = this.subtitles.iterator();
        while (iterator.hasNext()) {
            Title subtitle = (Title) iterator.next();
            if (subtitle instanceof LegendTitle) {
                if (seen == index) {
                    return (LegendTitle) subtitle;
                }
                else {
                    seen++;   
                }
            }
        }
        return null;        
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;   
        }
        if (!(obj instanceof LegendTitle)) {
            return false;   
        }
        if (!super.equals(obj)) {
            return false;   
        }
        LegendTitle that = (LegendTitle) obj;
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;   
        }
        if (this.legendItemGraphicEdge != that.legendItemGraphicEdge) {
            return false;   
        }
        if (this.legendItemGraphicAnchor != that.legendItemGraphicAnchor) {
            return false;   
        }
        if (this.legendItemGraphicLocation != that.legendItemGraphicLocation) {
            return false;   
        }
        if (!this.itemFont.equals(that.itemFont)) {
            return false;   
        }
        if (!this.itemPaint.equals(that.itemPaint)) {
            return false;   
        }
        if (!this.hLayout.equals(that.hLayout)) {
            return false;   
        }
        if (!this.vLayout.equals(that.vLayout)) {
            return false;   
        }
        return true;
    }
