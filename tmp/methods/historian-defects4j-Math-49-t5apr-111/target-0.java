    public OpenMapRealVector ebeDivide(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
     Iterator iter = this.getEntries().iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        }
        return res;
    }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
     Iterator iter = this.getEntries().iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
    public OpenMapRealVector ebeMultiply(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
     Iterator iter = this.getEntries().iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v[iter.key()]);
        }
        return res;
    }
    public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
     Iterator iter = this.getEntries().iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
        }
        return res;
    }
