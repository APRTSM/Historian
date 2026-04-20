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
                    i0 = 1 + i / 4;
                    flipIfWarranted(n, 2);
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
            if (dMin1 == dN1) {
				tau = 0.5 * dMin1;
			}
            dN   = work[j4p2 + 2];
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
