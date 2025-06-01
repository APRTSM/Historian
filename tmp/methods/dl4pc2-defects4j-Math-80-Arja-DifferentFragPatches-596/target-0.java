    public RealVector getEigenvector(final int i)
        throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
        if (eigenvectors == null) {
            findEigenVectors();
        }
        if (cachedVt == null) {
			if (eigenvectors == null) {
				findEigenVectors();
			}
			final int m = eigenvectors.length;
			cachedVt = MatrixUtils.createRealMatrix(m, m);
			for (int k = 0; k < m; ++k) {
				cachedVt.setRowVector(k, eigenvectors[k]);
			}
		}
		return eigenvectors[i].copy();
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
