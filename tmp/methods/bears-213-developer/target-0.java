    void setFirstRow(int firstRow) {
        if (queryType == QUERY) {
            ((Query) queryInstance).setFirstRow(firstRow);
            return;
        }
        throw new IllegalArgumentException("query not supported!");
    }
    void setMaxRows(int maxRows) {
        if (queryType == QUERY) {
            ((Query) queryInstance).setMaxRows(maxRows);
            return;
        }
        throw new IllegalArgumentException("query not supported!");
    }
