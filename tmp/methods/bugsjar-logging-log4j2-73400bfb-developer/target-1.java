    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        if (marker == null) {
            return null;
        } else if (marker instanceof Log4jMarker) {
            return ((Log4jMarker) marker).getLog4jMarker();
        } else {
            final Log4jMarkerFactory factory = (Log4jMarkerFactory) StaticMarkerBinder.SINGLETON.getMarkerFactory();
            return ((Log4jMarker) factory.getMarker(marker)).getLog4jMarker();
        }
    }
    private Marker addMarkerIfAbsent(final String name, final org.apache.logging.log4j.Marker log4jMarker) {
        final Marker marker = new Log4jMarker(log4jMarker);
        final Marker existing = markerMap.putIfAbsent(name, marker);
        return existing == null ? marker : existing;
    }
    private static org.apache.logging.log4j.Marker convertMarker(final Marker original) {
        if (original == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        final org.apache.logging.log4j.Marker marker = MarkerManager.getMarker(original.getName());
        if (original.hasReferences()) {
            final Iterator it = original.iterator();
            while (it.hasNext()) {
                final Marker next = (Marker) it.next();
                // kind of hope nobody uses cycles in their Markers. I mean, why would you do that?
                marker.addParents(convertMarker(next));
            }
        }
        return marker;
    }
    public Marker getMarker(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        Marker marker = markerMap.get(name);
        if (marker != null) {
            return marker;
        }
        final org.apache.logging.log4j.Marker log4jMarker = MarkerManager.getMarker(name);
        return addMarkerIfAbsent(name, log4jMarker);
    }
    public Marker getMarker(final Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        Marker m = markerMap.get(marker.getName());
        if (m != null) {
            return m;
        }
        return addMarkerIfAbsent(marker.getName(), convertMarker(marker));
    }
