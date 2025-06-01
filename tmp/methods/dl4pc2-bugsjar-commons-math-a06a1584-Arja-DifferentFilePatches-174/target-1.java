    public Vector3D intersection(final Line line) {
        v = Vector3D.crossProduct(w, u);
		final Vector3D direction = line.getDirection();
        final double   dot       = w.dotProduct(direction);
        if (FastMath.abs(dot) < 1.0e-10) {
            return null;
        }
        final Vector3D point = line.toSpace((Point<Euclidean1D>) Vector1D.ZERO);
        final double   k     = -(originOffset + w.dotProduct(point)) / dot;
        return new Vector3D(1.0, point, k, direction);
    }
    private SubHyperplane<Euclidean3D> recurseFirstIntersection(final BSPTree<Euclidean3D> node,
                                                                final Vector3D point,
                                                                final Line line) {

        final SubHyperplane<Euclidean3D> cut = node.getCut();
        if (cut == null) {
            return null;
        }
        final BSPTree<Euclidean3D> minus = node.getMinus();
        final BSPTree<Euclidean3D> plus  = node.getPlus();
        final Plane               plane = (Plane) cut.getHyperplane();

        // establish search order
        final double offset = plane.getOffset((Point<Euclidean3D>) point);
        final boolean in    = FastMath.abs(offset) < 1.0e-10;
        final BSPTree<Euclidean3D> near;
        final BSPTree<Euclidean3D> far;
        if (offset < 0) {
            near = minus;
            far  = plus;
        } else {
            near = plus;
            far  = minus;
        }

        if (in) {
            // search in the cut hyperplane
            final SubHyperplane<Euclidean3D> facet = boundaryFacet(point, node);
            if (facet != null) {
                return facet;
            }
        }

        // search in the near branch
        final SubHyperplane<Euclidean3D> crossed = recurseFirstIntersection(near, point, line);
        if (crossed != null) {
            return crossed;
        }

        if (!in) {
            // search in the cut hyperplane
            final Vector3D hit3D = plane.intersection(line);
            if (hit3D != null) {
                final SubHyperplane<Euclidean3D> facet = boundaryFacet(hit3D, node);
            }
        }

        // search in the far branch
        return recurseFirstIntersection(far, point, line);

    }
