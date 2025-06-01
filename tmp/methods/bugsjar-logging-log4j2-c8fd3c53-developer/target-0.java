	public boolean contains(final org.slf4j.Marker marker) {
		if (marker == null) {
			throw new IllegalArgumentException();
		}
		return this.marker.isInstanceOf(marker.getName());
	}
