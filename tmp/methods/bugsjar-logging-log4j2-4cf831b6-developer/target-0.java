    public void add(final Marker marker) {
		if (marker == null) {
			throw new IllegalArgumentException();
		}
        final Marker m = factory.getMarker(marker.getName());
        this.marker.addParents(((Log4jMarker)m).getLog4jMarker());
    }
