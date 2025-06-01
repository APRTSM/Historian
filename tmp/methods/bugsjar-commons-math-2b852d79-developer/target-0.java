    public RealMatrix computeCorrelationMatrix(final double[][] matrix) {
       return computeCorrelationMatrix(new BlockRealMatrix(matrix));
    }
    public SpearmansCorrelation(final RealMatrix dataMatrix, final RankingAlgorithm rankingAlgorithm) {
        this.rankingAlgorithm = rankingAlgorithm;
        this.data = rankTransform(dataMatrix);
        rankCorrelation = new PearsonsCorrelation(data);
    }
    private List<Integer> getNaNPositions(final double[] input) {
        final List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < input.length; i++) {
            if (Double.isNaN(input[i])) {
                positions.add(i);
            }
        }
        return positions;
    }
    private RealMatrix rankTransform(final RealMatrix matrix) {
        RealMatrix transformed = null;

        if (rankingAlgorithm instanceof NaturalRanking &&
                ((NaturalRanking) rankingAlgorithm).getNanStrategy() == NaNStrategy.REMOVED) {
            final Set<Integer> nanPositions = new HashSet<Integer>();
            for (int i = 0; i < matrix.getColumnDimension(); i++) {
                nanPositions.addAll(getNaNPositions(matrix.getColumn(i)));
            }

            // if we have found NaN values, we have to update the matrix size
            if (!nanPositions.isEmpty()) {
                transformed = new BlockRealMatrix(matrix.getRowDimension() - nanPositions.size(),
                                                  matrix.getColumnDimension());
                for (int i = 0; i < transformed.getColumnDimension(); i++) {
                    transformed.setColumn(i, removeValues(matrix.getColumn(i), nanPositions));
                }
            }
        }

        if (transformed == null) {
            transformed = matrix.copy();
        }

        for (int i = 0; i < transformed.getColumnDimension(); i++) {
            transformed.setColumn(i, rankingAlgorithm.rank(transformed.getColumn(i)));
        }

        return transformed;
    }
    public RealMatrix computeCorrelationMatrix(final RealMatrix matrix) {
        final RealMatrix matrixCopy = rankTransform(matrix);
        return new PearsonsCorrelation().computeCorrelationMatrix(matrixCopy);
    }
    public double correlation(final double[] xArray, final double[] yArray) {
        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
        } else if (xArray.length < 2) {
            throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION,
                                                   xArray.length, 2);
        } else {
            double[] x = xArray;
            double[] y = yArray;
            if (rankingAlgorithm instanceof NaturalRanking &&
                NaNStrategy.REMOVED == ((NaturalRanking) rankingAlgorithm).getNanStrategy()) {
                final Set<Integer> nanPositions = new HashSet<Integer>();

                nanPositions.addAll(getNaNPositions(xArray));
                nanPositions.addAll(getNaNPositions(yArray));

                x = removeValues(xArray, nanPositions);
                y = removeValues(yArray, nanPositions);
            }
            return new PearsonsCorrelation().correlation(rankingAlgorithm.rank(x), rankingAlgorithm.rank(y));
        }
    }
    private double[] removeValues(final double[] input, final Set<Integer> indices) {
        if (indices.isEmpty()) {
            return input;
        }
        final double[] result = new double[input.length - indices.size()];
        for (int i = 0, j = 0; i < input.length; i++) {
            if (!indices.contains(i)) {
                result[j++] = input[i];
            }
        }
        return result;
    }
