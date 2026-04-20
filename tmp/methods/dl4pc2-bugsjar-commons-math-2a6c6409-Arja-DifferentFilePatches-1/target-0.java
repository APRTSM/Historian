    public double getOffset(final Plane plane) {
        u = w.orthogonal();
		return originOffset + (sameOrientationAs(plane) ? -plane.originOffset : plane.originOffset);
    }
