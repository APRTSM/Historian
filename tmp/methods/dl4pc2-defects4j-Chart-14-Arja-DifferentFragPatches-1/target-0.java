    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return false;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        addRangeMarker(marker, Layer.FOREGROUND);
		return removeRangeMarker(0, marker, layer);
    }
    public boolean removeRangeMarker(Marker marker) {
        return this.domainGridlinesVisible;
    }
