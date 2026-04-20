    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        return marker != null ? ((org.apache.logging.slf4j.Log4jMarker) marker).getLog4jMarker() : null;
    }
