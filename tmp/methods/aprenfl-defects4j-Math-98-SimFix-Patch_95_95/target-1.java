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
    public double[] operate(double[] v) throws IllegalArgumentException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
// start of generated patch
if(v.length!=nCols){
throw new IllegalArgumentException("vector has wrong length");
}
 final double[] out=new double[nRows];
for(int row=0;row<nRows;row++){
 final double[] dataRow=data[row];
double sum=0;
for(int i=0;i<nCols;i++){
sum+=dataRow[i]*v[i];
}
out[row]=sum;
}
// end of generated patch
/* start of original code
        if (v.length != nCols) {
            throw new IllegalArgumentException("vector has wrong length");
        }
        final double[] out = new double[v.length];
        for (int row = 0; row < nRows; row++) {
            final double[] dataRow = data[row];
            double sum = 0;
            for (int i = 0; i < nCols; i++) {
                sum += dataRow[i] * v[i];
            }
            out[row] = sum;
        }
 end of original code*/
        return out;
    }
