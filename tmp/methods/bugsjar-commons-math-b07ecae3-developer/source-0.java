    public Weight(double[] weight) {
        final int dim = weight.length;
        weightMatrix = MatrixUtils.createRealMatrix(dim, dim);
        for (int i = 0; i < dim; i++) {
            weightMatrix.setEntry(i, i, weight[i]);
        }
    }
