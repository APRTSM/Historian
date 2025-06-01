    public boolean removeRangeMarker(Marker marker) {
        return false;
    }
    public ValueAxis getRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.rangeAxes.size()) {
            result = (ValueAxis) this.rangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            this.domainGridlineStroke = FastScatterPlot.DEFAULT_GRIDLINE_STROKE;
			if (parent instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) parent;
                result = cp.getRangeAxis(index);
            }
        }
        return result;
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return false;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.domainCrosshairVisible;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return false;
    }
