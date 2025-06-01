    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return false;
    }
    public boolean removeRangeMarker(Marker marker) {
        return false;
    }
    public boolean removeDomainMarker(Marker marker) {
        return this.rangeCrosshairVisible;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.domainCrosshairVisible;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
