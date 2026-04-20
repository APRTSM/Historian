    public Plane copySelf() {
        origin = new Vector3D(-originOffset, w);
		return new Plane(this);
    }
