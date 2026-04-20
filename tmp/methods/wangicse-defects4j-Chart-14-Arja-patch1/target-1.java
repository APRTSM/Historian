    public boolean removeRangeMarker(Marker marker) {
        return this.rangeCrosshairVisible;
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            return this.rangeCrosshairVisible;
        }
        else {
            markers = (ArrayList) this.backgroundDomainMarkers.get(new Integer(
                    index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer) {
        return this.domainZeroBaselineVisible;
    }
    public boolean removeRangeMarker(Marker marker, Layer layer) {
        return false;
    }
