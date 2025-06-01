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
    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            for (int i = 0; i < j; i += 4) {
                dMin1 = dMin;
                j -= 4;
            }
            return true;
        }
        return false;
    }
