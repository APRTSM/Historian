    public RealMatrix computeCorrelationMatrix(double[][] matrix) {
       return computeCorrelationMatrix(new BlockRealMatrix(matrix));
    }
    public double correlation(final double[] xArray, final double[] yArray) {
        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
        } else if (xArray.length < 2) {
            throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION,
                                                   xArray.length, 2);
        } else {
            return new PearsonsCorrelation().correlation(rankingAlgorithm.rank(xArray),
                    rankingAlgorithm.rank(yArray));
        }
    }
    public SpearmansCorrelation(final RealMatrix dataMatrix, final RankingAlgorithm rankingAlgorithm) {
        this.data = dataMatrix.copy();
        this.rankingAlgorithm = rankingAlgorithm;
        rankTransform(data);
        rankCorrelation = new PearsonsCorrelation(data);
    }
    public RealMatrix computeCorrelationMatrix(RealMatrix matrix) {
        RealMatrix matrixCopy = matrix.copy();
        rankTransform(matrixCopy);
        return new PearsonsCorrelation().computeCorrelationMatrix(matrixCopy);
    }
    private void rankTransform(RealMatrix matrix) {
        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            matrix.setColumn(i, rankingAlgorithm.rank(matrix.getColumn(i)));
        }
    }
