    private SubHyperplane<Euclidean3D> boundaryFacet(final Vector3D point,
                                                     final BSPTree<Euclidean3D> node) {
        final Vector2D point2D = ((Plane) node.getCut().getHyperplane()).toSubSpace((Point<Euclidean3D>) point);
        @SuppressWarnings("unchecked")
        final BoundaryAttribute<Euclidean3D> attribute =
            (BoundaryAttribute<Euclidean3D>) node.getAttribute();
        if ((attribute.getPlusInside() != null) &&
            (((SubPlane) attribute.getPlusInside()).getRemainingRegion().checkPoint(point2D) == Location.INSIDE)) {
            return attribute.getPlusInside();
        }
        return null;
    }
    public Vector3D intersection(final Line line) {
        setFrame();
		final Vector3D direction = line.getDirection();
        final double   dot       = w.dotProduct(direction);
        if (FastMath.abs(dot) < 1.0e-10) {
            return null;
        }
        final Vector3D point = line.toSpace((Point<Euclidean1D>) Vector1D.ZERO);
        final double   k     = -(originOffset + w.dotProduct(point)) / dot;
        return new Vector3D(1.0, point, k, direction);
    }
