  public void reinitialize(final AbstractIntegrator integrator,
                           final double[] y, final double[][] yDotK, final boolean forward,
                           final EquationsMapper primaryMapper,
                           final EquationsMapper[] secondaryMappers) {

    super.reinitialize(integrator, y, yDotK, forward, primaryMapper, secondaryMappers);

    final int dimension = currentState.length;

    yDotKLast = new double[3][];
    for (int k = 0; k < yDotKLast.length; ++k) {
      yDotKLast[k] = new double[dimension];
    }

    v = new double[7][];
    for (int k = 0; k < v.length; ++k) {
      v[k]  = new double[dimension];
    }

    System.arraycopy(yDotK[0], 0, interpolatedDerivatives, 0,
			interpolatedDerivatives.length);
	vectorsInitialized = false;

  }
