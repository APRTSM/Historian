    public void setLimit(long limit) {
        this.limit = limit;
        left.setLimit(limit);
        right.setLimit(limit);
    }
