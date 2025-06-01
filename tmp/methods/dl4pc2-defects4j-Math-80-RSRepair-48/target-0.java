    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            for (int i = 0; i < j; i += 4) {
                work[j + 2] = 0.0;
                tau = g * dMin;
            }
            return true;
        }
        return false;
    }
