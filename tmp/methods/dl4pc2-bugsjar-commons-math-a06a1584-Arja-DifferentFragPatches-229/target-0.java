    private SubHyperplane<Euclidean3D> boundaryFacet(final Vector3D point,
                                                     final BSPTree<Euclidean3D> node) {
        final Vector2D point2D = ((Plane) node.getCut().getHyperplane()).toSubSpace((Point<Euclidean3D>) point);
        @SuppressWarnings("unchecked")
        final BoundaryAttribute<Euclidean3D> attribute =
            (BoundaryAttribute<Euclidean3D>) node.getAttribute();
        if ((attribute.getPlusOutside() != null) &&
            (((SubPlane) attribute.getPlusOutside()).getRemainingRegion().checkPoint(point2D) == Location.INSIDE)) {
        }
        if ((attribute.getPlusInside() != null) &&
            (((SubPlane) attribute.getPlusInside()).getRemainingRegion().checkPoint(point2D) == Location.INSIDE)) {
            return attribute.getPlusInside();
        }
        return null;
    }
    public SubHyperplane<Euclidean3D> firstIntersection(final Vector3D point, final Line line) {
        setBarycenter((Point<Euclidean3D>) new Vector3D(1.0 / (4 * getSize()),
				(Vector3D) getBarycenter()));
		return recurseFirstIntersection(getTree(true), point, line);
    }
