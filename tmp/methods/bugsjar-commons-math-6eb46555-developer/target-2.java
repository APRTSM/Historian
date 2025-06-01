    public double dotProduct(RealVector v) {
        if (v instanceof ArrayRealVector) {
            final double[] vData = ((ArrayRealVector) v).data;
            checkVectorDimensions(vData.length);
            double dot = 0;
            for (int i = 0; i < data.length; i++) {
                dot += data[i] * vData[i];
            }
            return dot;
        }
        return super.dotProduct(v);
    }
    public double dotProduct(RealVector v) {
        checkVectorDimensions(v);
        double d = 0;
        final int n = getDimension();
        for (int i = 0; i < n; i++) {
            d += getEntry(i) * v.getEntry(i);
        }
        return d;
    }
