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
    public double dotProduct(RealVector v) {
        if(v instanceof OpenMapRealVector) {
            return dotProduct((OpenMapRealVector)v);
        } else {
            return super.dotProduct(v);
        }
    }
    public double dotProduct(OpenMapRealVector v) {
        checkVectorDimensions(v.getDimension());
        boolean thisIsSmaller  = entries.size() < v.entries.size();
        Iterator iter = thisIsSmaller  ? entries.iterator() : v.entries.iterator();
        OpenIntToDoubleHashMap larger = thisIsSmaller  ? v.entries : entries;
        double d = 0;
        while(iter.hasNext()) {
            iter.advance();
            d += iter.value() * larger.get(iter.key());
        }
        return d;
    }
