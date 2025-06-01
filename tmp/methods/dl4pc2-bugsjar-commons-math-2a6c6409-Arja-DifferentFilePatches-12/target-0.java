    public PolyhedronsSet wholeSpace() {
        v = Vector3D.crossProduct(w, u);
		return new PolyhedronsSet(tolerance);
    }
