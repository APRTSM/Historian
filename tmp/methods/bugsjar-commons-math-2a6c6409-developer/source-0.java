    private static BSPTree<Euclidean3D> buildBoundary(final double xMin, final double xMax,
                                                      final double yMin, final double yMax,
                                                      final double zMin, final double zMax,
                                                      final double tolerance) {
        final Plane pxMin = new Plane(new Vector3D(xMin, 0,    0),   Vector3D.MINUS_I, tolerance);
        final Plane pxMax = new Plane(new Vector3D(xMax, 0,    0),   Vector3D.PLUS_I,  tolerance);
        final Plane pyMin = new Plane(new Vector3D(0,    yMin, 0),   Vector3D.MINUS_J, tolerance);
        final Plane pyMax = new Plane(new Vector3D(0,    yMax, 0),   Vector3D.PLUS_J,  tolerance);
        final Plane pzMin = new Plane(new Vector3D(0,    0,   zMin), Vector3D.MINUS_K, tolerance);
        final Plane pzMax = new Plane(new Vector3D(0,    0,   zMax), Vector3D.PLUS_K,  tolerance);
        @SuppressWarnings("unchecked")
        final Region<Euclidean3D> boundary =
        new RegionFactory<Euclidean3D>().buildConvex(pxMin, pxMax, pyMin, pyMax, pzMin, pzMax);
        return boundary.getTree(false);
    }
