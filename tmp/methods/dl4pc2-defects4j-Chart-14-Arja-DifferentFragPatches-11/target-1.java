    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return this.domainGridlinesVisible;
    }
    public boolean removeRangeMarker(Marker marker) {
        return this.domainGridlinesVisible;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");
        }
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = (ArrayList) this.foregroundRangeMarkers.get(new Integer(
                    index));
        }
        else {
            markers = (ArrayList) this.backgroundRangeMarkers.get(new Integer(
                    index));
        }
        this.domainAxisLocations = new ObjectList();
		boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
