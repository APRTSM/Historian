    public void setSubMatrix(double[][] subMatrix, int row, int column) 
        throws MatrixIndexException {
        if ((row < 0) || (column < 0)){
            throw new MatrixIndexException
                ("invalid row or column index selection");          
        }
        final int nRows = subMatrix.length;
        if (nRows == 0) {
            throw new IllegalArgumentException(
            "Matrix must have at least one row."); 
        }
        final int nCols = subMatrix[0].length;
        if (nCols == 0) {
            throw new IllegalArgumentException(
            "Matrix must have at least one column."); 
        }
        for (int r = 1; r < nRows; r++) {
            if (subMatrix[r].length != nCols) {
                throw new IllegalArgumentException(
                "All input rows must have the same length.");
            }
        }       
        if (data == null) {
            if ((row > 0)||(column > 0)) throw new MatrixIndexException
                ("matrix must be initialized to perfom this method");
            data = new double[nRows][nCols];
            System.arraycopy(subMatrix, 0, data, 0, subMatrix.length);          
        }   
        if (((nRows + row) > this.getRowDimension()) ||
            (nCols + column > this.getColumnDimension()))
            throw new MatrixIndexException(
                    "invalid row or column index selection");                   
        for (int i = 0; i < nRows; i++) {
            System.arraycopy(subMatrix[i], 0, data[row + i], column, nCols);
        } 
        lu = null;
    }
