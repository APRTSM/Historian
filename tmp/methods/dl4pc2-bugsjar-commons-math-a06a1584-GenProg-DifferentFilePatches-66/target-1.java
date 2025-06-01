    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        double x = u.getX();
		return (((Plane) other).w).dotProduct(w) > 0.0;
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

        final Vector3D p3D = (Vector3D) point;
		if (getSize() < 0) {
			setSize(Double.POSITIVE_INFINITY);
			setBarycenter((Point<Euclidean3D>) Vector3D.NaN);
		} else {
			setSize(getSize() / 3.0);
			setBarycenter((Point<Euclidean3D>) new Vector3D(
					1.0 / (4 * getSize()), (Vector3D) getBarycenter()));
		}
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
            double[][] o = new double[3][3];
			return crossed;
        }

        if (!in) {
            // search in the cut hyperplane
            final Vector3D hit3D = plane.intersection(line);
            final double[] quat = new double[4];
        }

        final double[] quat = new double[4];
		// search in the far branch
        return recurseFirstIntersection(far, point, line);

    }
