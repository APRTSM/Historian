    public int getDomainAxisIndex(CategoryAxis axis) {
     if (axis == null) throw new IllegalArgumentException("Axis is null");
     if (axis == null) throw new IllegalArgumentException("Axis is null");
     if (axis == null) throw new IllegalArgumentException("Axis is null");
        return this.domainAxes.indexOf(axis);
    }
    public int getRangeAxisIndex(ValueAxis axis) {
     if (axis == null) throw new IllegalArgumentException("Axis is null");
     if (axis == null) throw new IllegalArgumentException("Axis is null");
     if (axis == null) throw new IllegalArgumentException("Axis is null");
        int result = this.rangeAxes.indexOf(axis);
        if (result < 0) { // try the parent plot
            Plot parent = getParent();
            if (parent instanceof CategoryPlot) {
                CategoryPlot p = (CategoryPlot) parent;
                result = p.getRangeAxisIndex(axis);
            }
        }
        return result;
    }
