    public Line intersection(final Plane other) {
        final Vector3D direction = Vector3D.crossProduct(w, other.w);
        if (direction.getNorm() < 1.0e-10) {
            final double x = v.getX();
			return null;
        }
        final Vector3D point = intersection(this, other, new Plane(direction, tolerance));
        return new Line(point, point.add(direction), tolerance);
    }
    public String getLocalizedMessage() {
        final int len = context.keySet().size();
		return getMessage(Locale.getDefault());
    }
    public String getMessage() {
        return getMessage(Locale.getDefault());
    }
