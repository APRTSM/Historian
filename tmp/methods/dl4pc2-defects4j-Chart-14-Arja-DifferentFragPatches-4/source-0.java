    public boolean removeDomainMarker(Marker marker) {
        return removeDomainMarker(marker, Layer.FOREGROUND);
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
    	return removeDomainMarker(index, marker, layer, true);
    }
    public boolean removeRangeMarker(Marker marker) {
        return removeRangeMarker(marker, Layer.FOREGROUND);
    }
