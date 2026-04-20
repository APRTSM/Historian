    public void setLimit(long limit) {
        this.limit = limit;
        applyLimitOffset();
    }
    private void applyLimitOffset() {
        long subqueryLimit = QueryImpl.saturatedAdd(limit, offset);
        left.setLimit(subqueryLimit);
        right.setLimit(subqueryLimit);
    }
    public void setOffset(long offset) {
        this.offset = offset;
        applyLimitOffset();
    }
