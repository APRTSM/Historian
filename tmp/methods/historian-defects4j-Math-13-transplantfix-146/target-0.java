    private RealMatrix squareRoot(RealMatrix m) {
            if (m instanceof DiagonalMatrix) {
final int dim = m.getRowDimension();
final RealMatrix sqrtM=new DiagonalMatrix(dim);
int i = 0;while (i < dim) {
sqrtM.setEntry(i,i,FastMath.sqrt(m.getEntry(i,i)));
i++;}
return sqrtM;
}

final EigenDecomposition dec = new EigenDecomposition(m);
            return dec.getSquareRoot();
    }
