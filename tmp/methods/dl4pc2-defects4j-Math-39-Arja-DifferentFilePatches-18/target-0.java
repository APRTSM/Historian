  public void storeTime(final double t) {
    if (v == null) {
		v = new double[7][];
		for (int k = 0; k < 7; ++k) {
			v[k] = new double[interpolatedState.length];
		}
	}
	super.storeTime(t);
    vectorsInitialized = false;
  }
