    public double getConditionNumber() {
        return singularValues[0] / singularValues[singularValues.length - 1];
    }
        private Solver(final double[] singularValues, final RealMatrix uT,
                       final RealMatrix v, final boolean nonSingular) {
            double[][] suT = uT.getData();
            for (int i = 0; i < singularValues.length; ++i) {
                final double a;
                if (singularValues[i] > 0) {
                 a = 1 / singularValues[i];
                } else {
                 a = 0;
                }
                final double[] suTi = suT[i];
                for (int j = 0; j < suTi.length; ++j) {
                    suTi[j] *= a;
                }
            }
            pseudoInverse = v.multiply(new Array2DRowRealMatrix(suT, false));
            this.nonSingular = nonSingular;
        }
    public RealMatrix getCovariance(final double minSingularValue) {
        // get the number of singular values to consider
        final int p = singularValues.length;
        int dimension = 0;
        while ((dimension < p) && (singularValues[dimension] >= minSingularValue)) {
            ++dimension;
        }

        if (dimension == 0) {
            throw new NumberIsTooLargeException(LocalizedFormats.TOO_LARGE_CUTOFF_SINGULAR_VALUE,
                                                minSingularValue, singularValues[0], true);
        }

        final double[][] data = new double[dimension][p];
        getVT().walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
            /** {@inheritDoc} */
            @Override
            public void visit(final int row, final int column,
                    final double value) {
                data[row][column] = value / singularValues[row];
            }
        }, 0, dimension - 1, 0, p - 1);

        RealMatrix jv = new Array2DRowRealMatrix(data, false);
        return jv.transpose().multiply(jv);
    }
    public int getRank() {
        final double threshold = FastMath.max(m, n) * FastMath.ulp(singularValues[0]);

        for (int i = singularValues.length - 1; i >= 0; --i) {
            if (singularValues[i] > threshold) {
                return i + 1;
            }
        }
        return 0;
    }
    public SingularValueDecompositionImpl(final RealMatrix matrix) {
        m = matrix.getRowDimension();
        n = matrix.getColumnDimension();

        cachedU = null;
        cachedS = null;
        cachedV = null;
        cachedVt = null;

        double[][] localcopy = matrix.getData();
        double[][] matATA = new double[n][n];
        //
        // create A^T*A
        //
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                matATA[i][j] = 0.0;
                for (int k = 0; k < m; k++) {
                    matATA[i][j] += localcopy[k][i] * localcopy[k][j];
                }
                matATA[j][i] = matATA[i][j];
            }
        }

        double[][] matAAT = new double[m][m];
        //
        // create A*A^T
        //
        for (int i = 0; i < m; i++) {
            for (int j = i; j < m; j++) {
                matAAT[i][j] = 0.0;
                for (int k = 0; k < n; k++) {
                    matAAT[i][j] += localcopy[i][k] * localcopy[j][k];
                }
                 matAAT[j][i] = matAAT[i][j];
            }
        }
        int p;
        if (m >= n) {
            p = n;
            // compute eigen decomposition of A^T*A
            eigenDecomposition
                = new EigenDecompositionImpl(new Array2DRowRealMatrix(matATA), 1);
            singularValues = eigenDecomposition.getRealEigenvalues();
            cachedV = eigenDecomposition.getV();
            // compute eigen decomposition of A*A^T
            eigenDecomposition
                = new EigenDecompositionImpl(new Array2DRowRealMatrix(matAAT), 1);
            cachedU = eigenDecomposition.getV().getSubMatrix(0, m - 1, 0, p - 1);
        } else {
            p = m;
            // compute eigen decomposition of A*A^T
            eigenDecomposition
                = new EigenDecompositionImpl(new Array2DRowRealMatrix(matAAT), 1);
            singularValues = eigenDecomposition.getRealEigenvalues();
            cachedU = eigenDecomposition.getV();

            // compute eigen decomposition of A^T*A
            eigenDecomposition
                = new EigenDecompositionImpl(new Array2DRowRealMatrix(matATA), 1);
            cachedV = eigenDecomposition.getV().getSubMatrix(0, n - 1 , 0, p - 1);
        }
        for (int i = 0; i < p; i++) {
            singularValues[i] = FastMath.sqrt(FastMath.abs(singularValues[i]));
        }
        // Up to this point, U and V are computed independently of each other.
        // There still a sign indetermination of each column of, say, U.
        // The sign is set such that A.V_i=sigma_i.U_i (i<=p)
        // The right sign corresponds to a positive dot product of A.V_i and U_i
        for (int i = 0; i < p; i++) {
            RealVector tmp = cachedU.getColumnVector(i);
            double product=matrix.operate(cachedV.getColumnVector(i)).dotProduct(tmp);
            if (product < 0) {
                cachedU.setColumnVector(i, tmp.mapMultiply(-1));
            }
        }
    }
