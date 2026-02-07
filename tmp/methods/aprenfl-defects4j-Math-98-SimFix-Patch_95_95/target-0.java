    public BigDecimal[] operate(BigDecimal[] v) throws IllegalArgumentException {
        if (v.length != this.getColumnDimension()) {
            throw new IllegalArgumentException("vector has wrong length");
        }
// start of generated patch
 final int nRows=this.getRowDimension();
 final int nCols=this.getColumnDimension();
 final BigDecimal[] out=new BigDecimal[nRows];
// end of generated patch
/* start of original code
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        final BigDecimal[] out = new BigDecimal[v.length];
 end of original code*/
        for (int row = 0; row < nRows; row++) {
            BigDecimal sum = ZERO;
            for (int i = 0; i < nCols; i++) {
                sum = sum.add(data[row][i].multiply(v[i]));
            }
            out[row] = sum;
        }
        return out;
    }
