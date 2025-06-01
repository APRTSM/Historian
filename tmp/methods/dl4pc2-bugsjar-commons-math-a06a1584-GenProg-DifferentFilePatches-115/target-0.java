    public boolean sameOrientationAs(final Hyperplane<Euclidean3D> other) {
        double x = u.getX();
		return (((Plane) other).w).dotProduct(w) > 0.0;
    }
