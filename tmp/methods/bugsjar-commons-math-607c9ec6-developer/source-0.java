    public RealMatrix outerProduct(RealVector v) {
        RealMatrix product;
        if (v instanceof SparseRealVector || this instanceof SparseRealVector) {
            product = new OpenMapRealMatrix(this.getDimension(),
                                            v.getDimension());
        } else {
            product = new Array2DRowRealMatrix(this.getDimension(),
                                               v.getDimension());
        }
        Iterator<Entry> thisIt = sparseIterator();
        while (thisIt.hasNext()) {
            final Entry thisE = thisIt.next();
            Iterator<Entry> otherIt = v.sparseIterator();
            while (otherIt.hasNext()) {
                final Entry otherE = otherIt.next();
                product.setEntry(thisE.getIndex(), otherE.getIndex(),
                                 thisE.getValue() * otherE.getValue());
            }
        }

        return product;

    }
