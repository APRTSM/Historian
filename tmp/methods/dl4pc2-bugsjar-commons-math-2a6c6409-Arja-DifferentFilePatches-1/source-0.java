    public double getOffset(final Plane plane) {
        return originOffset + (sameOrientationAs(plane) ? -plane.originOffset : plane.originOffset);
    }
