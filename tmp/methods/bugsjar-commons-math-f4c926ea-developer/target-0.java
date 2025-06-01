    private static Line[] boxBoundary(final double xMin, final double xMax,
                                      final double yMin, final double yMax,
                                      final double tolerance) {
        if ((xMin >= xMax - tolerance) || (yMin >= yMax - tolerance)) {
            // too thin box, build an empty polygons set
            return null;
        }
        final Vector2D minMin = new Vector2D(xMin, yMin);
        final Vector2D minMax = new Vector2D(xMin, yMax);
        final Vector2D maxMin = new Vector2D(xMax, yMin);
        final Vector2D maxMax = new Vector2D(xMax, yMax);
        return new Line[] {
            new Line(minMin, maxMin, tolerance),
            new Line(maxMin, maxMax, tolerance),
            new Line(maxMax, minMax, tolerance),
            new Line(minMax, minMin, tolerance)
        };
    }
