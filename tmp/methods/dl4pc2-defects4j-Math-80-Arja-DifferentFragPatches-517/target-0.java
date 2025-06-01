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
    public RealVector getEigenvector(final int i)
        throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
        dMin = 0.0;
		if (eigenvectors == null) {
            findEigenVectors();
        }
        return eigenvectors[i].copy();
    }
