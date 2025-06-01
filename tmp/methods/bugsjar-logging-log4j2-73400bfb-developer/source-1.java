    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        return marker != null ? ((org.apache.logging.slf4j.Log4jMarker) marker).getLog4jMarker() : null;
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
        marker = new Log4jMarker(log4jMarker);
        final Marker existing = markerMap.putIfAbsent(name, marker);
        return existing == null ? marker : existing;
    }
