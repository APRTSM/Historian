    public SubPlane wholeHyperplane() {
        origin = new Vector3D(-originOffset, w);
		return new SubPlane(this, new PolygonsSet(tolerance));
    }
