    public boolean contains(final org.slf4j.Marker marker) {
        return this.marker.isInstanceOf(marker.getName());
    }
