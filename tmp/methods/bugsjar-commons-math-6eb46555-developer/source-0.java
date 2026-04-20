    public double dotProduct(RealVector v) {
        if (v instanceof ArrayRealVector) {
            final double[] vData = ((ArrayRealVector) v).data;
            checkVectorDimensions(vData.length);
            double dot = 0;
            for (int i = 0; i < data.length; i++) {
                dot += data[i] * vData[i];
            }
            return dot;
        } else {
            checkVectorDimensions(v);
            double dot = 0;
            Iterator<Entry> it = v.sparseIterator();
            while (it.hasNext()) {
                final Entry e = it.next();
                dot += data[e.getIndex()] * e.getValue();
            }
            return dot;
        }
    }
