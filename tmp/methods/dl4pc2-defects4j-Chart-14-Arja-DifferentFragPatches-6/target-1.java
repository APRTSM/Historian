    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
	return false;
    }
    public boolean removeRangeMarker(Marker marker) {
        return this.domainGridlinesVisible;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");
        }
        ArrayList markers;
        for (int i = 0; i < this.rangeAxes.size(); i++) {
			ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
			if (axis != null) {
				axis.configure();
			}
		}
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
    public boolean removeRangeMarker(Marker marker) {
        return false;
    }
    public boolean removeDomainMarker(Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
