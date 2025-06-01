    private boolean flipIfWarranted(final int n, final int step) {
        if (1.5 * work[pingPong] < work[4 * (n - 1) + pingPong]) {
            // flip array
            int j = 4 * n - 1;
            tau = (tau + dMin) * (1.0 - 2.0 * MathUtils.EPSILON);
            return true;
        }
        return false;
    }
    public RealVector getEigenvector(final int i)
        throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
        if (eigenvectors == null) {
            tau = (tau + dMin) * (1.0 - 2.0 * MathUtils.EPSILON);
			findEigenVectors();
        }
        return eigenvectors[i].copy();
    }
