    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            int j = realEigenvalues.length - 1;
            dMin2 = 0;
            return true;
        }
        return false;
    }
