    public PieSectionLabelGenerator getLabelGenerator() {
        if (direction == null) {
			throw new IllegalArgumentException("Null 'direction' argument.");
		}
		return this.labelGenerator;
    }
