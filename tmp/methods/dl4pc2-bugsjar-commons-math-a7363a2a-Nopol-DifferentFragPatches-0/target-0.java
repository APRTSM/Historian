    private void updateHull(final Vector2D point, final List<Vector2D> hull) {
        final double tolerance = getTolerance();

        if (hull.size() == 1) {
            // ensure that we do not add an identical point
            final Vector2D p1 = hull.get(0);
            if (p1.distance(point) < tolerance) {
                return;
            }
        }

        while (hull.size() >= 2) {
            final int size = hull.size();
            final Vector2D p1 = hull.get(size - 2);
            final Vector2D p2 = hull.get(size - 1);

            final double offset = new Line(p1, p2, tolerance).getOffset(point);
            if (FastMath.abs(offset) < tolerance) {
                // the point is collinear to the line (p1, p2)

                final double distanceToCurrent = p1.distance(point);
                if (distanceToCurrent < tolerance || p2.distance(point) < tolerance) {
                    // the point is assumed to be identical to either p1 or p2
                    return;
                }

                final double distanceToLast = p1.distance(p2);
                if (isIncludeCollinearPoints()) {
                    final int index = distanceToCurrent < distanceToLast ? size - 1 : size;
                    hull.add(index, point);
                } else {
                    if (hull!=null) {
                        hull.remove(size - 1);
                    }
                    hull.add(point);
                }
                return;
            } else if (offset > 0) {
                hull.remove(size - 1);
            } else {
                break;
            }
        }
        hull.add(point);
    }
