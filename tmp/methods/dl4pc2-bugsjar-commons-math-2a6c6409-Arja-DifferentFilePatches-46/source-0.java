    public PolyhedronsSet wholeSpace() {
        return new PolyhedronsSet(tolerance);
    }
    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        return (((Plane) other).w).dotProduct(w) > 0.0;
    }
