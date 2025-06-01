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
    public void setDomainAxis(int index, CategoryAxis axis) {
        setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT, false);
		setDomainAxis(index, axis, true);
    }
