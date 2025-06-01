    private void dqd(final int start, final int end) {

        eMin = work[4 * start + pingPong + 4];
        double d = work[4 * start + pingPong];
        dMin = d;

        if (pingPong == 0) {
            for (int j4 = 4 * start + 3; j4 < 4 * (end - 3); j4 += 4) {
                work[j4 - 2] = d + work[j4 - 1];
                if (work[j4 - 2] == 0.0) {
                    work[j4] = 0.0;
                    d = work[j4 + 1];
                    dMin = d;
                    eMin = 0.0;
                } else if ((MathUtils.SAFE_MIN * work[j4 + 1] < work[j4 - 2]) &&
                           (MathUtils.SAFE_MIN * work[j4 - 2] < work[j4 + 1])) {
                    final double tmp = work[j4 + 1] / work[j4 - 2];
                    work[j4] = work[j4 - 1] * tmp;
                    d *= tmp;
                } else {
                    work[j4] = work[j4 + 1] * (work[j4 - 1] / work[j4 - 2]);
                    d *= work[j4 + 1] / work[j4 - 2];
                }
                dMin = Math.min(dMin, d);
                eMin = Math.min(eMin, work[j4]);
            }
        } else {
            for (int j4 = 4 * start + 3; j4 < 4 * (end - 3); j4 += 4) {
                work[j4 - 3] = d + work[j4];
                if (work[j4 - 3] == 0.0) {
                    work[j4 - 1] = 0.0;
                    d = work[j4 + 2];
                    dMin = d;
                    eMin = 0.0;
                } else if ((MathUtils.SAFE_MIN * work[j4 + 2] < work[j4 - 3]) &&
                           (MathUtils.SAFE_MIN * work[j4 - 3] < work[j4 + 2])) {
                    final double tmp = work[j4 + 2] / work[j4 - 3];
                    work[j4 - 1] = work[j4] * tmp;
                    d *= tmp;
                } else {
                    work[j4 - 1] = work[j4 + 2] * (work[j4] / work[j4 - 3]);
                    d *= work[j4 + 2] / work[j4 - 3];
                }
                dMin = Math.min(dMin, d);
                eMin = Math.min(eMin, work[j4 - 1]);
            }
        }

        // Unroll last two steps
        dN2   = d;
        dMin2 = dMin;
        int j4 = 4 * (end - 2) - pingPong - 1;
        int j4p2 = j4 + 2 * pingPong - 1;
        work[j4 - 2] = dN2 + work[j4p2];
        if (work[j4 - 2] == 0.0) {
            work[j4] = 0.0;
            dN1  = work[j4p2 + 2];
            dMin = dN1;
            eMin = 0.0;
        } else if ((MathUtils.SAFE_MIN * work[j4p2 + 2] < work[j4 - 2]) &&
                   (MathUtils.SAFE_MIN * work[j4 - 2] < work[j4p2 + 2])) {
            final double tmp = work[j4p2 + 2] / work[j4 - 2];
            work[j4] = work[j4p2] * tmp;
            dN1 = dN2 * tmp;
        } else {
            work[j4] = work[j4p2 + 2] * (work[j4p2] / work[j4 - 2]);
            dN1 = work[j4p2 + 2] * (dN2 / work[j4 - 2]);
        }
        dMin = Math.min(dMin, dN1);

        dMin1 = dMin;
        j4 = j4 + 4;
        j4p2 = j4 + 2 * pingPong - 1;
        work[j4 - 2] = dN1 + work[j4p2];
        if (work[j4 - 2] == 0.0) {
            int begin = 0;
            dMin = dN;
            eMin = 0.0;
        } else if ((MathUtils.SAFE_MIN * work[j4p2 + 2] < work[j4 - 2]) &&
                   (MathUtils.SAFE_MIN * work[j4 - 2] < work[j4p2 + 2])) {
            final double tmp = work[j4p2 + 2] / work[j4 - 2];
            work[j4] = work[j4p2] * tmp;
            dN = dN1 * tmp;
        } else {
            work[j4] = work[j4p2 + 2] * (work[j4p2] / work[j4 - 2]);
            dN = work[j4p2 + 2] * (dN1 / work[j4 - 2]);
        }
        dMin = Math.min(dMin, dN);

        work[j4 + 2] = dN;
        work[4 * end - pingPong - 1] = eMin;

    }
    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            for (int i = 0; i < j; i += 4) {
                j -= 4;
            }
            return true;
        }
        return false;
    }
    private int goodStep(final int start, final int end) {

        g = 0.0;

        // step 1: accepting realEigenvalues
        int deflatedEnd = end;
        for (boolean deflating = true; deflating;) {

            if (start >= deflatedEnd) {
                // the array has been completely deflated
                return deflatedEnd;
            }

            final int k = 4 * deflatedEnd + pingPong - 1;

            if ((start == deflatedEnd - 1) ||
                ((start != deflatedEnd - 2) &&
                 ((work[k - 5] <= TOLERANCE_2 * (sigma + work[k - 3])) ||
                  (work[k - 2 * pingPong - 4] <= TOLERANCE_2 * work[k - 7])))) {

                // one eigenvalue found, deflate array
                work[4 * deflatedEnd - 4] = sigma + work[4 * deflatedEnd - 4 + pingPong];
                deflatedEnd -= 1;

            } else if ((start == deflatedEnd - 2) ||
                (work[k - 9] <= TOLERANCE_2 * sigma) ||
                (work[k - 2 * pingPong - 8] <= TOLERANCE_2 * work[k - 11])) {

                // two realEigenvalues found, deflate array
                if (work[k - 3] > work[k - 7]) {
                    final double tmp = work[k - 3];
                    work[k - 3] = work[k - 7];
                    work[k - 7] = tmp;
                }

                if (work[k - 5] > TOLERANCE_2 * work[k - 3]) {
                    double t = 0.5 * ((work[k - 7] - work[k - 3]) + work[k - 5]);
                    double s = work[k - 3] * (work[k - 5] / t);
                    if (s <= t) {
                        s = work[k - 3] * work[k - 5] / (t * (1 + Math.sqrt(1 + s / t)));
                    } else {
                        s = work[k - 3] * work[k - 5] / (t + Math.sqrt(t * (t + s)));
                    }
                    t = work[k - 7] + (s + work[k - 5]);
                    work[k - 3] *= work[k - 7] / t;
                    work[k - 7]  = t;
                }
                work[4 * deflatedEnd - 8] = sigma + work[k - 7];
                work[4 * deflatedEnd - 4] = sigma + work[k - 3];
                deflatedEnd -= 2;
            } else {

                // no more realEigenvalues found, we need to iterate
                deflating = false;

            }

        }

        final int l = 4 * deflatedEnd + pingPong - 1;

        // step 2: flip array if needed
        if ((dMin <= 0) || (deflatedEnd < end)) {
            if (flipIfWarranted(deflatedEnd, 1)) {
                dMin2 = Math.min(dMin2, work[l - 1]);
                work[l - 1] =
                    Math.min(work[l - 1],
                             Math.min(work[3 + pingPong], work[7 + pingPong]));
                dMin  = -0.0;
            }
        }

        if ((dMin < 0) ||
            (MathUtils.SAFE_MIN * qMax < Math.min(work[l - 1],
                                                  Math.min(work[l - 9],
                                                           dMin2 + work[l - 2 * pingPong])))) {
            // step 3: choose a shift
            computeShiftIncrement(start, deflatedEnd, end - deflatedEnd);

            // step 4a: dqds
            for (boolean loop = true; loop;) {

                // perform one dqds step with the chosen shift
                dqds(start, deflatedEnd);

                // check result of the dqds step
                if ((dMin >= 0) && (dMin1 > 0)) {
                    // the shift was good
                    updateSigma(tau);
                    return deflatedEnd;
                } else if ((dMin < 0.0) &&
                           (dMin1 > 0.0) &&
                           (work[4 * deflatedEnd - 5 - pingPong] < TOLERANCE * (sigma + dN1)) &&
                           (Math.abs(dN) < TOLERANCE * sigma)) {
                   // convergence hidden by negative DN.
                    work[4 * deflatedEnd - 3 - pingPong] = 0.0;
                    updateSigma(tau);
                    return deflatedEnd;
                } else if (dMin < 0.0) {
                    // tau too big. Select new tau and try again.
                    if (tType < -22) {
                        // failed twice. Play it safe.
                        tau = 0.0;
                    } else if (dMin1 > 0.0) {
                        // late failure. Gives excellent shift.
                        tau = (tau + dMin) * (1.0 - 2.0 * MathUtils.EPSILON);
                        tType -= 11;
                    } else {
                        // early failure. Divide by 4.
                        tau *= 0.25;
                        tType -= 12;
                    }
                } else if (Double.isNaN(dMin)) {
                    tau = 0.0;
                } else {
                    // possible underflow. Play it safe.
                    loop = false;
                }
            }

        }

        // perform a dqd step (i.e. no shift)
        dqd(start, deflatedEnd);

        return deflatedEnd;

    }
    public RealVector getEigenvector(final int i)
        throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
        if (cachedD == null) {
				cachedD = MatrixUtils.createRealDiagonalMatrix(realEigenvalues);
			}
		if (eigenvectors == null) {
            findEigenVectors();
        }
        return eigenvectors[i].copy();
    }
    private void initialSplits(final int n) {

        pingPong = 0;
        for (int k = 0; k < 2; ++k) {

            // apply Li's reverse test
            double d = work[4 * (n - 1) + pingPong];
            for (int i = 4 * (n - 2) + pingPong; i >= 0; i -= 4) {
                if (work[i + 2] <= TOLERANCE_2 * d) {
                    work[i + 2] = -0.0;
                    d = work[i];
                } else {
                    d *= work[i] / (d + work[i + 2]);
                }
            }

            // apply dqd plus Li's forward test.
            d = work[pingPong];
            for (int i = 2 + pingPong; i < 4 * n - 2; i += 4) {
                final int j = i - 2 * pingPong - 1;
                work[j] = d + work[i];
                if (work[i] <= TOLERANCE_2 * d) {
                    work[j]     = d;
                    d = work[i + 2];
                } else if ((MathUtils.SAFE_MIN * work[i + 2] < work[j]) &&
                           (MathUtils.SAFE_MIN * work[j] < work[i + 2])) {
                    final double tmp = work[i + 2] / work[j];
                    work[j + 2] = work[i] * tmp;
                    d *= tmp;
                } else {
                    work[j + 2] = work[i + 2] * (work[i] / work[j]);
                    d *= work[i + 2] / work[j];
               }
            }
            work[4 * n - 3 - pingPong] = d;

            // from ping to pong
            pingPong = 1 - pingPong;

        }

    }
