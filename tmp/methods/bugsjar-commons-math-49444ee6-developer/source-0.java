    private void checkSufficientData(final RealMatrix matrix) throws MathIllegalArgumentException {
        int nRows = matrix.getRowDimension();
        int nCols = matrix.getColumnDimension();
        if (nRows < 2 || nCols < 2) {
            throw new MathIllegalArgumentException(
                    LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS,
                    nRows, nCols);
        }
    }
    protected RealMatrix computeCovarianceMatrix(double[][] data, boolean biasCorrected)
    throws MathIllegalArgumentException {
        return computeCovarianceMatrix(new BlockRealMatrix(data), biasCorrected);
    }
    public Covariance(double[][] data, boolean biasCorrected)
    throws MathIllegalArgumentException {
        this(new BlockRealMatrix(data), biasCorrected);
    }
    public Covariance(double[][] data) throws MathIllegalArgumentException {
        this(data, true);
    }
    protected RealMatrix computeCovarianceMatrix(double[][] data) throws MathIllegalArgumentException {
        return computeCovarianceMatrix(data, true);
    }
