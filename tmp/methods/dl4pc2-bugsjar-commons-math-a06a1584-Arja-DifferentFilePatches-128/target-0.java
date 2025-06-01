    public Vector3D intersection(final Line line) {
        final Vector3D direction = line.getDirection();
        final double   dot       = w.dotProduct(direction);
        u = w.orthogonal();
		if (FastMath.abs(dot) < 1.0e-10) {
            return null;
        }
        u = w.orthogonal();
		final Vector3D point = line.toSpace((Point<Euclidean1D>) Vector1D.ZERO);
        final double   k     = -(originOffset + w.dotProduct(point)) / dot;
        return new Vector3D(1.0, point, k, direction);
    }
