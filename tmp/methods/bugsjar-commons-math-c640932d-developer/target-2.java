    private void findEigenVectors(double[][] householderMatrix) {

        double[][]z = householderMatrix.clone();
        final int n = main.length;
        realEigenvalues = new double[n];
        imagEigenvalues = new double[n];
        double[] e = new double[n];
        for (int i = 0; i < n - 1; i++) {
            realEigenvalues[i] = main[i];
            e[i] = secondary[i];
        }
        realEigenvalues[n - 1] = main[n - 1];
        e[n - 1] = 0.0;

        // Determine the largest main and secondary value in absolute term.
        double maxAbsoluteValue=0.0;
        for (int i = 0; i < n; i++) {
            if (Math.abs(realEigenvalues[i])>maxAbsoluteValue) {
                maxAbsoluteValue=Math.abs(realEigenvalues[i]);
            }
            if (Math.abs(e[i])>maxAbsoluteValue) {
                maxAbsoluteValue=Math.abs(e[i]);
            }
        }
        // Make null any main and secondary value too small to be significant
        if (maxAbsoluteValue!=0.0) {
            for (int i=0; i < n; i++) {
                if (Math.abs(realEigenvalues[i])<=MathUtils.EPSILON*maxAbsoluteValue) {
                    realEigenvalues[i]=0.0;
                }
                if (Math.abs(e[i])<=MathUtils.EPSILON*maxAbsoluteValue) {
                    e[i]=0.0;
                }
            }
        }

        for (int j = 0; j < n; j++) {
            int its = 0;
            int m;
            do {
                for (m = j; m < n - 1; m++) {
                    double delta = Math.abs(realEigenvalues[m]) + Math.abs(realEigenvalues[m + 1]);
                    if (Math.abs(e[m]) + delta == delta) {
                        break;
                    }
                }
                if (m != j) {
                    if (its == maxIter)
                        throw new InvalidMatrixException(
                                new MaxIterationsExceededException(maxIter));
                    its++;
                    double q = (realEigenvalues[j + 1] - realEigenvalues[j]) / (2 * e[j]);
                    double t = Math.sqrt(1 + q * q);
                    if (q < 0.0) {
                        q = realEigenvalues[m] - realEigenvalues[j] + e[j] / (q - t);
                    } else {
                        q = realEigenvalues[m] - realEigenvalues[j] + e[j] / (q + t);
                    }
                    double u = 0.0;
                    double s = 1.0;
                    double c = 1.0;
                    int i;
                    for (i = m - 1; i >= j; i--) {
                        double p = s * e[i];
                        double h = c * e[i];
                        if (Math.abs(p) >= Math.abs(q)) {
                            c = q / p;
                            t = Math.sqrt(c * c + 1.0);
                            e[i + 1] = p * t;
                            s = 1.0 / t;
                            c = c * s;
                        } else {
                            s = p / q;
                            t = Math.sqrt(s * s + 1.0);
                            e[i + 1] = q * t;
                            c = 1.0 / t;
                            s = s * c;
                        }
                        if (e[i + 1] == 0.0) {
                            realEigenvalues[i + 1] -= u;
                            e[m] = 0.0;
                            break;
                        }
                        q = realEigenvalues[i + 1] - u;
                        t = (realEigenvalues[i] - q) * s + 2.0 * c * h;
                        u = s * t;
                        realEigenvalues[i + 1] = q + u;
                        q = c * t - h;
                        for (int ia = 0; ia < n; ia++) {
                            p = z[ia][i + 1];
                            z[ia][i + 1] = s * z[ia][i] + c * p;
                            z[ia][i] = c * z[ia][i] - s * p;
                        }
                    }
                    if (t == 0.0 && i >= j)
                        continue;
                    realEigenvalues[j] -= u;
                    e[j] = q;
                    e[m] = 0.0;
                }
            } while (m != j);
        }

        //Sort the eigen values (and vectors) in increase order
        for (int i = 0; i < n; i++) {
            int k = i;
            double p = realEigenvalues[i];
            for (int j = i + 1; j < n; j++) {
                if (realEigenvalues[j] > p) {
                    k = j;
                    p = realEigenvalues[j];
                }
            }
            if (k != i) {
                realEigenvalues[k] = realEigenvalues[i];
                realEigenvalues[i] = p;
                for (int j = 0; j < n; j++) {
                    p = z[j][i];
                    z[j][i] = z[j][k];
                    z[j][k] = p;
                }
            }
        }

        // Determine the largest eigen value in absolute term.
        maxAbsoluteValue=0.0;
        for (int i = 0; i < n; i++) {
            if (Math.abs(realEigenvalues[i])>maxAbsoluteValue) {
                maxAbsoluteValue=Math.abs(realEigenvalues[i]);
            }
        }
        // Make null any eigen value too small to be significant
        if (maxAbsoluteValue!=0.0) {
            for (int i=0; i < n; i++) {
                if (Math.abs(realEigenvalues[i])<MathUtils.EPSILON*maxAbsoluteValue) {
                    realEigenvalues[i]=0.0;
                }
            }
        }
        eigenvectors = new ArrayRealVector[n];
        double[] tmp = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tmp[j] = z[j][i];
            }
            eigenvectors[i] = new ArrayRealVector(tmp);
        }
    }
    public SingularValueDecompositionImpl(final RealMatrix matrix)
            throws InvalidMatrixException {

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
                matATA[j][i]=matATA[i][j];
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
                 matAAT[j][i]=matAAT[i][j];
            }
        }
        int p;
        if (m>=n) {
            p=n;
            // compute eigen decomposition of A^T*A
            eigenDecomposition = new EigenDecompositionImpl(
                    new Array2DRowRealMatrix(matATA),1.0);
            singularValues = eigenDecomposition.getRealEigenvalues();
            cachedV = eigenDecomposition.getV();
            // compute eigen decomposition of A*A^T
            eigenDecomposition = new EigenDecompositionImpl(
                    new Array2DRowRealMatrix(matAAT),1.0);
            cachedU = eigenDecomposition.getV().getSubMatrix(0, m - 1, 0, p - 1);
        } else {
            p=m;
            // compute eigen decomposition of A*A^T
            eigenDecomposition = new EigenDecompositionImpl(
                    new Array2DRowRealMatrix(matAAT),1.0);
            singularValues = eigenDecomposition.getRealEigenvalues();
            cachedU = eigenDecomposition.getV();

            // compute eigen decomposition of A^T*A
            eigenDecomposition = new EigenDecompositionImpl(
                    new Array2DRowRealMatrix(matATA),1.0);
            cachedV = eigenDecomposition.getV().getSubMatrix(0,n-1,0,p-1);
        }
        for (int i = 0; i < p; i++) {
            singularValues[i] = Math.sqrt(Math.abs(singularValues[i]));
        }
        // Up to this point, U and V are computed independently of each other.
        // There still a sign indetermination of each column of, say, U.
        // The sign is set such that A.V_i=sigma_i.U_i (i<=p)
        // The right sign corresponds to a positive dot product of A.V_i and U_i
        for (int i = 0; i < p; i++) {
          RealVector tmp = cachedU.getColumnVector(i);
          double product=matrix.operate(cachedV.getColumnVector(i)).dotProduct(tmp);
          if (product<0) {
            cachedU.setColumnVector(i, tmp.mapMultiply(-1.0));
          }
        }
    }
    public double getRMS() {
        return Math.sqrt(getChiSquare() / rows);
    }
    public double getChiSquare() {
        double chiSquare = 0;
        for (int i = 0; i < rows; ++i) {
            final double residual = residuals[i];
            chiSquare += residual * residual * residualsWeights[i];
        }
        return chiSquare;
    }
