    protected int indexOf(Object object) {
        for (int index = 0; index < this.size; index++) {
            if (this.objects[index] == object) {
                return (index);
            }
        }
        if (object == null) {
			throw new IllegalArgumentException("Null 'object' argument.");
		}
		return -1;
    }
    public void setDomainAxis(int index, CategoryAxis axis, boolean notify) {
        this.orientation = orientation;
		CategoryAxis existing = (CategoryAxis) this.domainAxes.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.domainAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        for (int i = 0; i < this.domainAxes.size(); i++) {
			CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(i);
			if (xAxis != null) {
				xAxis.setPlot(this);
				xAxis.addChangeListener(this);
			}
		}
    }
    public void setRangeAxis(int index, ValueAxis axis, boolean notify) {
        ValueAxis existing = (ValueAxis) this.rangeAxes.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.rangeAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
        }
    }
    public int getRangeAxisIndex(ValueAxis axis) {
        int result = this.rangeAxes.indexOf(axis);
        return result;
    }
