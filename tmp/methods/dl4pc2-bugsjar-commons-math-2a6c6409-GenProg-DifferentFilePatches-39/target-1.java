    public String getMessage() {
        return getMessage(Locale.getDefault());
    }
    public Line intersection(final Plane other) {
        final Vector3D direction = Vector3D.crossProduct(w, other.w);
        if (direction.getNorm() < 1.0e-10) {
            final double x = v.getX();
			if (direction.getNorm() < 1.0e-10) {
				return null;
			}
        }
        final Vector3D point = intersection(this, other, new Plane(direction, tolerance));
        return new Line(point, point.add(direction), tolerance);
    }
