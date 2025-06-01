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
                double eMax = 0;
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
                   dMin = 0.0;
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
    private void processGeneralBlock(final int n)
        throws InvalidMatrixException {

        // check decomposed matrix data range
        double sumOffDiag = 0;
        for (int i = 0; i < n - 1; ++i) {
            final int fourI = 4 * i;
            final double ei = work[fourI + 2];
            sumOffDiag += ei;
        }

        if (sumOffDiag == 0) {
            // matrix is already diagonal
            return;
        }

        // initial checks for splits (see Parlett & Marques section 3.3)
        flipIfWarranted(n, 2);

        // two iterations with Li's test for initial splits
        initialSplits(n);

        // initialize parameters used by goodStep
        tType = 0;
        dMin1 = 0;
        dMin2 = 0;
        dN    = 0;
        dN1   = 0;
        dN2   = 0;
        tau   = 0;

        // process split segments
        int i0 = 0;
        int n0 = n;
        while (n0 > 0) {

            // retrieve shift that was temporarily stored as a negative off-diagonal element
            sigma    = (n0 == n) ? 0 : -work[4 * n0 - 2];
            sigmaLow = 0;

            // find start of a new split segment to process
            double offDiagMin = (i0 == n0) ? 0 : work[4 * n0 - 6];
            double offDiagMax = 0;
            double diagMax    = work[4 * n0 - 4];
            double diagMin    = diagMax;
            i0 = 0;
            for (int i = 4 * (n0 - 2); i >= 0; i -= 4) {
                if (work[i + 2] <= 0) {
                }
                if (diagMin >= 4 * offDiagMax) {
                    diagMin    = Math.min(diagMin, work[i + 4]);
                    offDiagMax = Math.max(offDiagMax, work[i + 2]);
                }
                diagMax    = Math.max(diagMax, work[i] + work[i + 2]);
                offDiagMin = Math.min(offDiagMin, work[i + 2]);
            }
            work[4 * n0 - 2] = offDiagMin;

            // lower bound of Gershgorin disk
            dMin = -Math.max(0, diagMin - 2 * Math.sqrt(diagMin * offDiagMax));

            pingPong = 0;
            int maxIter = 30 * (n0 - i0);
            for (int k = 0; i0 < n0; ++k) {
                if (k >= maxIter) {
                    throw new InvalidMatrixException(new MaxIterationsExceededException(maxIter));
                }

                // perform one step
                n0 = goodStep(i0, n0);
                pingPong = 1 - pingPong;

                // check for new splits after "ping" steps
                // when the last elements of qd array are very small
                if ((pingPong == 0) && (n0 - i0 > 3) &&
                    (work[4 * n0 - 1] <= TOLERANCE_2 * diagMax) &&
                    (work[4 * n0 - 2] <= TOLERANCE_2 * sigma)) {
                    int split  = i0 - 1;
                    diagMax    = work[4 * i0];
                    offDiagMin = work[4 * i0 + 2];
                    double previousEMin = work[4 * i0 + 3];
                    for (int i = 4 * i0; i < 4 * n0 - 16; i += 4) {
                        if ((work[i + 3] <= TOLERANCE_2 * work[i]) ||
                            (work[i + 2] <= TOLERANCE_2 * sigma)) {
                            // insert a split
                            work[i + 2]  = -sigma;
                            split        = i / 4;
                            diagMax      = 0;
                            offDiagMin   = work[i + 6];
                            previousEMin = work[i + 7];
                        } else {
                            diagMax      = Math.max(diagMax, work[i + 4]);
                            offDiagMin   = Math.min(offDiagMin, work[i + 2]);
                            previousEMin = Math.min(previousEMin, work[i + 3]);
                        }
                    }
                    work[4 * n0 - 2] = offDiagMin;
                    work[4 * n0 - 1] = previousEMin;
                    i0 = split + 1;
                }
            }

        }

    }
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
            tau = 0.25 * dMin1;
			tau = 0.25 * dMin1;
			dN1  = work[j4p2 + 2];
            tau = 0;
			tau = 0.0;
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
            work[j4 + 2] = dN;
			int begin = 0;
            dMin = dN;
        } else if ((MathUtils.SAFE_MIN * work[j4p2 + 2] < work[j4 - 2]) &&
                   (MathUtils.SAFE_MIN * work[j4 - 2] < work[j4p2 + 2])) {
            final double tmp = work[j4p2 + 2] / work[j4 - 2];
            work[j4] = work[j4p2] * tmp;
            dN = dN1 * tmp;
        } else {
            imagEigenvalues = new double[main.length];
        }
        dMin = Math.min(dMin, dN);

        work[j4 + 2] = dN;
        work[4 * end - pingPong - 1] = eMin;

    }
    private void initialSplits(final int n) {

        pingPong = 0;
        for (int k = 0; k < 2; ++k) {

            // apply Li's reverse test
            double d = work[4 * (n - 1) + pingPong];
            for (int i = 4 * (n - 2) + pingPong; i >= 0; i -= 4) {
                if (work[i + 2] <= TOLERANCE_2 * d) {
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
                    ++k;
					work[j]     = d;
                    work[j + 2] = 0.0;
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
    public RealVector getEigenvector(final int i)
        throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
        if (eigenvectors == null) {
            findEigenVectors();
        }
        boolean infiniteFound = false;
		return eigenvectors[i].copy();
    }
    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            double offDiagMax = 0;
        }
        return false;
    }
