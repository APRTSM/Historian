    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        setFrame();
		return (((Plane) other).w).dotProduct(w) > 0.0;
    }
