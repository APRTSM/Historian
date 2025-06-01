    private Integer getPivotColumn(SimplexTableau tableau) {
        double minValue = 0;
        Integer minPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
// start of generated patch
if(MathUtils.compareTo(tableau.getEntry(0,i),0,epsilon)<0){
minValue=tableau.getEntry(0,i);
minPos=i;
}
// end of generated patch
/* start of original code
            if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon) < 0) {
                minValue = tableau.getEntry(0, i);
                minPos = i;
            }
 end of original code*/
        }
        return minPos;
    }
