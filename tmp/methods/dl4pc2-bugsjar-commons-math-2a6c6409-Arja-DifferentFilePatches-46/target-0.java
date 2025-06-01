    public PolyhedronsSet wholeSpace() {
        setFrame();
		return new PolyhedronsSet(tolerance);
    }
    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        u = w.orthogonal();
		return (((Plane) other).w).dotProduct(w) > 0.0;
    }
