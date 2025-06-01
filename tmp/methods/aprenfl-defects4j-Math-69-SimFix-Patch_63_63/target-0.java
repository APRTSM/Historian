    public RealMatrix getCorrelationPValues() throws MathException {
        TDistribution tDistribution = new TDistributionImpl(nObs - 2);
        int nVars = correlationMatrix.getColumnDimension();
        double[][] out = new double[nVars][nVars];
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < nVars; j++) {
// start of generated patch
if(i==j&&i<nVars){
out[i][j]=0.0;
}else {
double r=correlationMatrix.getEntry(i,j);
double t=Math.abs(r*Math.sqrt((nObs-2)/(1-r*r)));
out[i][j]=2*(1-tDistribution.cumulativeProbability(t));
}
// end of generated patch
/* start of original code
                if (i == j) {
                    out[i][j] = 0d;
                } else {
                    double r = correlationMatrix.getEntry(i, j);
                    double t = Math.abs(r * Math.sqrt((nObs - 2)/(1 - r * r)));
                    out[i][j] = 2 * (1 - tDistribution.cumulativeProbability(t));
                }
 end of original code*/
            }
        }
        return new BlockRealMatrix(out);
    }
