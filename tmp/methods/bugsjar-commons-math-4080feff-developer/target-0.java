    public ConvexHull2D(final Vector2D[] vertices, final double tolerance)
        throws MathIllegalArgumentException {

        // assign tolerance as it will be used by the isConvex method
        this.tolerance = tolerance;

        if (!isConvex(vertices)) {
            throw new MathIllegalArgumentException(LocalizedFormats.NOT_CONVEX);
        }

        this.vertices = vertices.clone();
    }
    private boolean isConvex(final Vector2D[] hullVertices) {
        if (hullVertices.length < 3) {
            return true;
        }

        int sign = 0;
        for (int i = 0; i < hullVertices.length; i++) {
            final Vector2D p1 = hullVertices[i == 0 ? hullVertices.length - 1 : i - 1];
            final Vector2D p2 = hullVertices[i];
            final Vector2D p3 = hullVertices[i == hullVertices.length - 1 ? 0 : i + 1];

            final Vector2D d1 = p2.subtract(p1);
            final Vector2D d2 = p3.subtract(p2);

            final double crossProduct = MathArrays.linearCombination(d1.getX(), d2.getY(), -d1.getY(), d2.getX());
            final int cmp = Precision.compareTo(crossProduct, 0.0, tolerance);
            // in case of collinear points the cross product will be zero
            if (cmp != 0.0) {
                if (sign != 0.0 && cmp != sign) {
                    return false;
                }
                sign = cmp;
            }
        }

        return true;
    }
