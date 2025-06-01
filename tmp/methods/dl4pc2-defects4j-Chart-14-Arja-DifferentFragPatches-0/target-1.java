    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return this.drawSharedDomainAxis;
    }
    public boolean removeRangeMarker(Marker marker) {
        return this.drawSharedDomainAxis;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        addDomainMarker(0, marker, layer);
		return removeRangeMarker(0, marker, layer);
    }
    public boolean removeRangeMarker(Marker marker) {
        return false;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
