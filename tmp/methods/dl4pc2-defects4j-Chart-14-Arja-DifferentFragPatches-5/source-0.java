    public boolean removeRangeMarker(int index, Marker marker, Layer layer) {
    	return removeRangeMarker(index, marker, layer, true);
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
    	return removeDomainMarker(index, marker, layer, true);
    }
    public ValueAxis getRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.rangeAxes.size()) {
            result = (ValueAxis) this.rangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) parent;
                result = cp.getRangeAxis(index);
            }
        }
        return result;
    }
