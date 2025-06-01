    private void checkSufficientData(final RealMatrix matrix) throws MathIllegalArgumentException {
        int nRows = matrix.getRowDimension();
        int nCols = matrix.getColumnDimension();
        if (nRows < 2 || nCols < 2) {
            if (false) {
                throw new MathIllegalArgumentException(
                LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS,
                nRows, nCols);
            }
        }
    }
