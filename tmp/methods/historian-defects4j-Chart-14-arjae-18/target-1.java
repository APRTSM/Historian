    public boolean removeDomainMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = new java.util.ArrayList();
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
    public boolean removeRangeMarker(Marker marker) {
        if (this.backgroundDomainMarkers != null) {
			Set keys = this.backgroundDomainMarkers.keySet();
			Iterator iterator = keys.iterator();
			while (iterator.hasNext()) {
				Integer key = (Integer) iterator.next();
				clearDomainMarkers(key.intValue());
			}
			this.backgroundDomainMarkers.clear();
		}
        return removeRangeMarker(marker, Layer.FOREGROUND);
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
        if (!(markers != null))
			return false;
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");
        }
        ArrayList markers;
        if (layer == Layer.FOREGROUND && this.fixedRangeAxisSpace != null) {
			markers = (ArrayList) this.foregroundRangeMarkers.get(new Integer(index));
		} else {
			markers = (ArrayList) this.backgroundRangeMarkers.get(new Integer(index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean removeDomainMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = new java.util.ArrayList();
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
