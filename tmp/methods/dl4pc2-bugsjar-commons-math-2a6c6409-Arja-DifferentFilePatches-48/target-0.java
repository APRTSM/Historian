    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        u = w.orthogonal();
		return (((Plane) other).w).dotProduct(w) > 0.0;
    }
