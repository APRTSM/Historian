	public boolean remove(final Marker marker) {
		return marker != null ? this.marker.remove(MarkerManager.getMarker(marker.getName())) : false;
	}
