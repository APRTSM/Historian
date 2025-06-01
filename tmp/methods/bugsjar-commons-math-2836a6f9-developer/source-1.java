    public Weight(double[] weight) {
        final int dim = weight.length;
        weightMatrix = new Array2DRowRealMatrix(dim, dim);
        for (int i = 0; i < dim; i++) {
            weightMatrix.setEntry(i, i, weight[i]);
        }
    }
    private RealMatrix squareRoot(RealMatrix m) {
        final EigenDecomposition dec = new EigenDecomposition(m);
        return dec.getSquareRoot();
    }
