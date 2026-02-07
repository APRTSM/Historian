    public int getDomainAxisIndex(CategoryAxis axis) {
        if (axis == null) {
    throw new IllegalArgumentException("Empty variable: " + axis);
}
return this.domainAxes.indexOf(axis);
    }
