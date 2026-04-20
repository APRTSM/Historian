    void setMaxRows(int maxRows) {
        if (queryType == QUERY) {
            ((Query) queryInstance).setMaxRows(maxRows);
        }
        throw new IllegalArgumentException("query not supported!");
    }
    void setFirstRow(int firstRow) {
        if (queryType == QUERY) {
            ((Query) queryInstance).setFirstRow(firstRow);
        }
        throw new IllegalArgumentException("query not supported!");
    }
