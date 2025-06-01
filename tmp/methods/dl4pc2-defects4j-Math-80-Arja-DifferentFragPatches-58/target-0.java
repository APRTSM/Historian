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
                    work[i]     = -0.0;
                    dMin1 = dMin;
                    work[j + 2] = 0.0;
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
            work[j4] = 0.0;
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
