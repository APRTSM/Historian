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
