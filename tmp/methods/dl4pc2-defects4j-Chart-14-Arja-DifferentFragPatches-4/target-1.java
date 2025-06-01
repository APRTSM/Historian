    public boolean removeRangeMarker(Marker marker) {
        return false;
    }
    public boolean removeDomainMarker(Marker marker) {
        this.domainAxisLocations = new ObjectList();
		return removeDomainMarker(marker, Layer.FOREGROUND);
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return this.domainGridlinesVisible;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return false;
    }
