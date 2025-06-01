    public SingularValueDecompositionImpl(final RealMatrix matrix, final int max)
        throws InvalidMatrixException {

        m = matrix.getRowDimension();
        n = matrix.getColumnDimension();

        cachedU  = null;
        cachedS  = null;
        cachedV  = null;
        cachedVt = null;

        // transform the matrix to bidiagonal
        transformer         = new BiDiagonalTransformer(matrix);
        mainBidiagonal      = transformer.getMainDiagonalRef();
        secondaryBidiagonal = transformer.getSecondaryDiagonalRef();

        // compute Bt.B (if upper diagonal) or B.Bt (if lower diagonal)
        mainTridiagonal      = new double[mainBidiagonal.length];
        secondaryTridiagonal = new double[mainBidiagonal.length - 1];
        double a = mainBidiagonal[0];
        mainTridiagonal[0] = a * a;
        for (int i = 1; i < mainBidiagonal.length; ++i) {
            final double b  = secondaryBidiagonal[i - 1];
            secondaryTridiagonal[i - 1] = a * b;
            a = mainBidiagonal[i];
            mainTridiagonal[i] = a * a + b * b;
        }

        // compute singular values
        eigenDecomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal,
                                       MathUtils.SAFE_MIN);
        final double[] eigenValues = eigenDecomposition.getRealEigenvalues();
        int p = Math.min(max, eigenValues.length);
        while ((p > 0) && (eigenValues[p - 1] <= 0)) {
            --p;
        }
        singularValues = new double[p];
        for (int i = 0; i < p; ++i) {
            singularValues[i] = Math.sqrt(eigenValues[i]);
        }

    }
        public double[] solve(final double[] b)
            throws IllegalArgumentException {
            return pseudoInverse.operate(b);
        }
        public RealMatrix getInverse() {
            return pseudoInverse;
        }
    public DecompositionSolver getSolver() {
        return new Solver(singularValues, getUT(), getV(),
                          getRank() == Math.max(m, n));
    }
    public RealMatrix getCovariance(final double minSingularValue) {

        // get the number of singular values to consider
        final int p = singularValues.length;
        int dimension = 0;
        while ((dimension < p) && (singularValues[dimension] >= minSingularValue)) {
            ++dimension;
        }

        if (dimension == 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "cutoff singular value is {0}, should be at most {1}",
                  minSingularValue, singularValues[0]);
        }

        final double[][] data = new double[dimension][p];
        getVT().walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
            /** {@inheritDoc} */
            @Override
            public void visit(final int row, final int column, final double value) {
                data[row][column] = value / singularValues[row];
            }
        }, 0, dimension - 1, 0, p - 1);

        RealMatrix jv = new Array2DRowRealMatrix(data, false);
        return jv.transpose().multiply(jv);

    }
    public RealMatrix getU()
        throws InvalidMatrixException {

        if (cachedU == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                final double[][] wData = new double[m][p];
                double[] ei1 = eData[0];
                for (int i = 0; i < p - 1; ++i) {
                    // compute W = B.E.S^(-1) where E is the eigenvectors matrix
                    final double mi = mainBidiagonal[i];
                    final double si = secondaryBidiagonal[i];
                    final double[] ei0 = ei1;
                    final double[] wi  = wData[i];
                    ei1 = eData[i + 1];
                    for (int j = 0; j < p; ++j) {
                        wi[j] = (mi * ei0[j] + si * ei1[j]) / singularValues[j];
                    }
                }
                // last row
                final double lastMain = mainBidiagonal[p - 1];
                final double[] wr1  = wData[p - 1];
                for (int j = 0; j < p; ++j) {
                    wr1[j] = ei1[j] * lastMain / singularValues[j];
                }
                for (int i = p; i < m; ++i) {
                    wData[i] = new double[p];
                }
                cachedU =
                    transformer.getU().multiply(MatrixUtils.createRealMatrix(wData));
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, m - 1, 0, p - 1);
                cachedU = transformer.getU().multiply(e);
            }

        }

        // return the cached matrix
        return cachedU;

    }
        public RealVector solve(final RealVector b)
            throws IllegalArgumentException {
            return pseudoInverse.operate(b);
        }
    public RealMatrix getV()
        throws InvalidMatrixException {

        if (cachedV == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, n - 1, 0, p - 1);
                cachedV = transformer.getV().multiply(e);
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                // compute W = Bt.E.S^(-1) where E is the eigenvectors matrix
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                final double[][] wData = new double[n][p];
                double[] ei1 = eData[0];
                for (int i = 0; i < p - 1; ++i) {
                    final double mi = mainBidiagonal[i];
                    final double si = secondaryBidiagonal[i];
                    final double[] ei0 = ei1;
                    final double[] wi  = wData[i];
                    ei1 = eData[i + 1];
                    for (int j = 0; j < p; ++j) {
                        wi[j] = (mi * ei0[j] + si * ei1[j]) / singularValues[j];
                    }
                }
                // last row
                final double lastMain = mainBidiagonal[p - 1];
                final double[] wr1  = wData[p - 1];
                for (int j = 0; j < p; ++j) {
                    wr1[j] = ei1[j] * lastMain / singularValues[j];
                }
                for (int i = p; i < n; ++i) {
                    wData[i] = new double[p];
                }
                cachedV =
                    transformer.getV().multiply(MatrixUtils.createRealMatrix(wData));
            }

        }

        // return the cached matrix
        return cachedV;

    }
    public SingularValueDecompositionImpl(final RealMatrix matrix)
        throws InvalidMatrixException {
        this(matrix, Math.min(matrix.getRowDimension(), matrix.getColumnDimension()));
    }
        public RealMatrix solve(final RealMatrix b)
            throws IllegalArgumentException {
            return pseudoInverse.multiply(b);
        }
        private Solver(final double[] singularValues, final RealMatrix uT, final RealMatrix v,
                       final boolean nonSingular) {
            double[][] suT      = uT.getData();
            for (int i = 0; i < singularValues.length; ++i) {
                final double a      = 1.0 / singularValues[i];
                final double[] suTi = suT[i];
                for (int j = 0; j < suTi.length; ++j) {
                    suTi[j] *= a;
                }
            }
            pseudoInverse    = v.multiply(new Array2DRowRealMatrix(suT, false));
            this.nonSingular = nonSingular;
        }
