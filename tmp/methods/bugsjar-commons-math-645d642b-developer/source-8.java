  protected void computeInterpolatedStateAndDerivatives(final double theta,
                                          final double oneMinusThetaH) {

    if (! vectorsInitialized) {

      if (v == null) {
        v = new double[7][];
        for (int k = 0; k < 7; ++k) {
          v[k] = new double[interpolatedState.length];
        }
      }

      // perform the last evaluations if they have not been done yet
      finalizeStep();

      // compute the interpolation vectors for this time step
      for (int i = 0; i < interpolatedState.length; ++i) {
          final double yDot1  = yDotK[0][i];
          final double yDot6  = yDotK[5][i];
          final double yDot7  = yDotK[6][i];
          final double yDot8  = yDotK[7][i];
          final double yDot9  = yDotK[8][i];
          final double yDot10 = yDotK[9][i];
          final double yDot11 = yDotK[10][i];
          final double yDot12 = yDotK[11][i];
          final double yDot13 = yDotK[12][i];
          final double yDot14 = yDotKLast[0][i];
          final double yDot15 = yDotKLast[1][i];
          final double yDot16 = yDotKLast[2][i];
          v[0][i] = B_01 * yDot1  + B_06 * yDot6 + B_07 * yDot7 +
                    B_08 * yDot8  + B_09 * yDot9 + B_10 * yDot10 +
                    B_11 * yDot11 + B_12 * yDot12;
          v[1][i] = yDot1 - v[0][i];
          v[2][i] = v[0][i] - v[1][i] - yDotK[12][i];
          for (int k = 0; k < D.length; ++k) {
              v[k+3][i] = D[k][0] * yDot1  + D[k][1]  * yDot6  + D[k][2]  * yDot7  +
                          D[k][3] * yDot8  + D[k][4]  * yDot9  + D[k][5]  * yDot10 +
                          D[k][6] * yDot11 + D[k][7]  * yDot12 + D[k][8]  * yDot13 +
                          D[k][9] * yDot14 + D[k][10] * yDot15 + D[k][11] * yDot16;
          }
      }

      vectorsInitialized = true;

    }

    final double eta      = 1 - theta;
    final double twoTheta = 2 * theta;
    final double theta2   = theta * theta;
    final double dot1 = 1 - twoTheta;
    final double dot2 = theta * (2 - 3 * theta);
    final double dot3 = twoTheta * (1 + theta * (twoTheta -3));
    final double dot4 = theta2 * (3 + theta * (5 * theta - 8));
    final double dot5 = theta2 * (3 + theta * (-12 + theta * (15 - 6 * theta)));
    final double dot6 = theta2 * theta * (4 + theta * (-15 + theta * (18 - 7 * theta)));

    for (int i = 0; i < interpolatedState.length; ++i) {
      interpolatedState[i] = currentState[i] -
                             oneMinusThetaH * (v[0][i] -
                                               theta * (v[1][i] +
                                                        theta * (v[2][i] +
                                                                 eta * (v[3][i] +
                                                                        theta * (v[4][i] +
                                                                                 eta * (v[5][i] +
                                                                                        theta * (v[6][i])))))));
      interpolatedDerivatives[i] =  v[0][i] + dot1 * v[1][i] + dot2 * v[2][i] +
                                    dot3 * v[3][i] + dot4 * v[4][i] +
                                    dot5 * v[5][i] + dot6 * v[6][i];
    }

  }
  public void integrate(final ExpandableStatefulODE equations, final double t)
      throws MathIllegalStateException, MathIllegalArgumentException {

    sanityChecks(equations, t);
    setEquations(equations);
    resetEvaluations();
    final boolean forward = t > equations.getTime();

    // create some internal working arrays
    final double[] y0  = equations.getCompleteState();
    final double[] y = y0.clone();
    final int stages = c.length + 1;
    final double[][] yDotK = new double[stages][y.length];
    final double[] yTmp    = new double[y.length];
    final double[] yDotTmp = new double[y.length];

    // set up an interpolator sharing the integrator arrays
    final RungeKuttaStepInterpolator interpolator = (RungeKuttaStepInterpolator) prototype.copy();
    interpolator.reinitialize(this, yTmp, yDotK, forward,
                              equations.getPrimaryMapper(), equations.getSecondaryMappers());
    interpolator.storeTime(equations.getTime());

    // set up integration control objects
    stepStart         = equations.getTime();
    double  hNew      = 0;
    boolean firstTime = true;
    for (StepHandler handler : stepHandlers) {
        handler.reset();
    }
    setStateInitialized(false);

    // main integration loop
    isLastStep = false;
    do {

      interpolator.shift();

      // iterate over step size, ensuring local normalized error is smaller than 1
      double error = 10;
      while (error >= 1.0) {

        if (firstTime || !fsal) {
          // first stage
          computeDerivatives(stepStart, y, yDotK[0]);
        }

        if (firstTime) {
          final double[] scale = new double[mainSetDimension];
          if (vecAbsoluteTolerance == null) {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = scalAbsoluteTolerance + scalRelativeTolerance * FastMath.abs(y[i]);
              }
          } else {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * FastMath.abs(y[i]);
              }
          }
          hNew = initializeStep(forward, getOrder(), scale,
                                stepStart, y, yDotK[0], yTmp, yDotK[1]);
          firstTime = false;
        }

        stepSize = hNew;

        // next stages
        for (int k = 1; k < stages; ++k) {

          for (int j = 0; j < y0.length; ++j) {
            double sum = a[k-1][0] * yDotK[0][j];
            for (int l = 1; l < k; ++l) {
              sum += a[k-1][l] * yDotK[l][j];
            }
            yTmp[j] = y[j] + stepSize * sum;
          }

          computeDerivatives(stepStart + c[k-1] * stepSize, yTmp, yDotK[k]);

        }

        // estimate the state at the end of the step
        for (int j = 0; j < y0.length; ++j) {
          double sum    = b[0] * yDotK[0][j];
          for (int l = 1; l < stages; ++l) {
            sum    += b[l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
        }

        // estimate the error at the end of the step
        error = estimateError(yDotK, y, yTmp, stepSize);
        if (error >= 1.0) {
          // reject the step and attempt to reduce error by stepsize control
          final double factor =
              FastMath.min(maxGrowth,
                           FastMath.max(minReduction, safety * FastMath.pow(error, exp)));
          hNew = filterStep(stepSize * factor, forward, false);
        }

      }

      // local error is small enough: accept the step, trigger events and step handlers
      interpolator.storeTime(stepStart + stepSize);
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
      stepStart = acceptStep(interpolator, y, yDotTmp, t);

      if (!isLastStep) {

          // prepare next step
          interpolator.storeTime(stepStart);

          if (fsal) {
              // save the last evaluation for the next step
              System.arraycopy(yDotTmp, 0, yDotK[0], 0, y0.length);
          }

          // stepsize control for next step
          final double factor =
              FastMath.min(maxGrowth, FastMath.max(minReduction, safety * FastMath.pow(error, exp)));
          final double  scaledH    = stepSize * factor;
          final double  nextT      = stepStart + scaledH;
          final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
          hNew = filterStep(scaledH, forward, nextIsLast);

          final double  filteredNextT      = stepStart + hNew;
          final boolean filteredNextIsLast = forward ? (filteredNextT >= t) : (filteredNextT <= t);
          if (filteredNextIsLast) {
              hNew = t - stepStart;
          }

      }

    } while (!isLastStep);

    // dispatch results
    equations.setTime(stepStart);
    equations.setCompleteState(y);

    resetInternalState();

  }
  public void integrate(final ExpandableStatefulODE equations, final double t)
      throws MathIllegalStateException, MathIllegalArgumentException {

    sanityChecks(equations, t);
    setEquations(equations);
    resetEvaluations();
    final boolean forward = t > equations.getTime();

    // create some internal working arrays
    final double[] y0      = equations.getCompleteState();
    final double[] y       = y0.clone();
    final int stages       = c.length + 1;
    final double[][] yDotK = new double[stages][];
    for (int i = 0; i < stages; ++i) {
      yDotK [i] = new double[y0.length];
    }
    final double[] yTmp    = new double[y0.length];
    final double[] yDotTmp = new double[y0.length];

    // set up an interpolator sharing the integrator arrays
    final RungeKuttaStepInterpolator interpolator = (RungeKuttaStepInterpolator) prototype.copy();
    interpolator.reinitialize(this, yTmp, yDotK, forward,
                              equations.getPrimaryMapper(), equations.getSecondaryMappers());
    interpolator.storeTime(equations.getTime());

    // set up integration control objects
    stepStart = equations.getTime();
    stepSize  = forward ? step : -step;
    for (StepHandler handler : stepHandlers) {
        handler.reset();
    }
    setStateInitialized(false);

    // main integration loop
    isLastStep = false;
    do {

      interpolator.shift();

      // first stage
      computeDerivatives(stepStart, y, yDotK[0]);

      // next stages
      for (int k = 1; k < stages; ++k) {

          for (int j = 0; j < y0.length; ++j) {
              double sum = a[k-1][0] * yDotK[0][j];
              for (int l = 1; l < k; ++l) {
                  sum += a[k-1][l] * yDotK[l][j];
              }
              yTmp[j] = y[j] + stepSize * sum;
          }

          computeDerivatives(stepStart + c[k-1] * stepSize, yTmp, yDotK[k]);

      }

      // estimate the state at the end of the step
      for (int j = 0; j < y0.length; ++j) {
          double sum    = b[0] * yDotK[0][j];
          for (int l = 1; l < stages; ++l) {
              sum    += b[l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
      }

      // discrete events handling
      interpolator.storeTime(stepStart + stepSize);
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
      stepStart = acceptStep(interpolator, y, yDotTmp, t);

      if (!isLastStep) {

          // prepare next step
          interpolator.storeTime(stepStart);

          // stepsize control for next step
          final double  nextT      = stepStart + stepSize;
          final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
          if (nextIsLast) {
              stepSize = t - stepStart;
          }
      }

    } while (!isLastStep);

    // dispatch results
    equations.setTime(stepStart);
    equations.setCompleteState(y);

    stepStart = Double.NaN;
    stepSize  = Double.NaN;

  }
