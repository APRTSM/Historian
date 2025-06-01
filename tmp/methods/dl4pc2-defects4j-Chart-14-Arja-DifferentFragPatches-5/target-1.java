    public ValueAxis getRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.rangeAxes.size()) {
            result = (ValueAxis) this.rangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (orientation == null) {
				throw new IllegalArgumentException(
						"Null 'orientation' argument.");
			}
			if (parent instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) parent;
                result = cp.getRangeAxis(index);
            }
        }
        return result;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer) {
	return this.drawSharedDomainAxis;
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return this.rangeCrosshairVisible;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.rangeCrosshairVisible;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
