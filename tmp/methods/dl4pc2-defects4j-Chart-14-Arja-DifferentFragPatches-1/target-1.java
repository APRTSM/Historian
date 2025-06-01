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
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer) {
	return this.rangeCrosshairVisible;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (index < 0 || index >= getDatasetCount()) {
			throw new IllegalArgumentException("Index " + index
					+ " out of bounds.");
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
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
