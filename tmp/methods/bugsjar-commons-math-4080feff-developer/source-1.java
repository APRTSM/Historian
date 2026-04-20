    private boolean isConvex(final Vector2D[] hullVertices) {
        if (hullVertices.length < 3) {
            return true;
        }

        double sign = 0.0;
        for (int i = 0; i < hullVertices.length; i++) {
            final Vector2D p1 = hullVertices[i == 0 ? hullVertices.length - 1 : i - 1];
            final Vector2D p2 = hullVertices[i];
            final Vector2D p3 = hullVertices[i == hullVertices.length - 1 ? 0 : i + 1];

            final Vector2D d1 = p2.subtract(p1);
            final Vector2D d2 = p3.subtract(p2);

            final double cross = FastMath.signum(MathArrays.linearCombination( d1.getX(), d2.getY(),
                                                                              -d1.getY(), d2.getX()));
            // in case of collinear points the cross product will be zero
            if (cross != 0.0) {
                if (sign != 0.0 && cross != sign) {
                    return false;
                }
                sign = cross;
            }
        }

        return true;
    }
    public ConvexHull2D(final Vector2D[] vertices, final double tolerance)
        throws MathIllegalArgumentException {

        if (!isConvex(vertices)) {
            throw new MathIllegalArgumentException(LocalizedFormats.NOT_CONVEX);
        }

        this.vertices = vertices.clone();
        this.tolerance = tolerance;
    }
    public Collection<Vector2D> findHullVertices(final Collection<Vector2D> points) {

        final List<Vector2D> pointsSortedByXAxis = new ArrayList<Vector2D>(points);

        // sort the points in increasing order on the x-axis
        Collections.sort(pointsSortedByXAxis, new Comparator<Vector2D>() {
            public int compare(final Vector2D o1, final Vector2D o2) {
                final int diff = (int) FastMath.signum(o1.getX() - o2.getX());
                if (diff == 0) {
                    return (int) FastMath.signum(o1.getY() - o2.getY());
                } else {
                    return diff;
                }
            }
        });

        // build lower hull
        final List<Vector2D> lowerHull = new ArrayList<Vector2D>();
        for (Vector2D p : pointsSortedByXAxis) {
            updateHull(p, lowerHull);
        }

        // build upper hull
        final List<Vector2D> upperHull = new ArrayList<Vector2D>();
        for (int idx = pointsSortedByXAxis.size() - 1; idx >= 0; idx--) {
            final Vector2D p = pointsSortedByXAxis.get(idx);
            updateHull(p, upperHull);
        }

        // concatenate the lower and upper hulls
        // the last point of each list is omitted as it is repeated at the beginning of the other list
        final List<Vector2D> hullVertices = new ArrayList<Vector2D>(lowerHull.size() + upperHull.size() - 2);
        for (int idx = 0; idx < lowerHull.size() - 1; idx++) {
            hullVertices.add(lowerHull.get(idx));
        }
        for (int idx = 0; idx < upperHull.size() - 1; idx++) {
            hullVertices.add(upperHull.get(idx));
        }

        // special case: if the lower and upper hull may contain only 1 point if all are identical
        if (hullVertices.isEmpty() && ! lowerHull.isEmpty()) {
            hullVertices.add(lowerHull.get(0));
        }

        return hullVertices;
    }
