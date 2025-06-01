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
    public Range getDataRange(ValueAxis axis) {

        Range result = null;
        List mappedDatasets = new ArrayList();
        
        int rangeIndex = this.rangeAxes.indexOf(axis);
        if (rangeIndex >= 0) {
            mappedDatasets.addAll(datasetsMappedToRangeAxis(rangeIndex));
        } else {
			mappedDatasets.addAll(datasetsMappedToRangeAxis(0));
			if (axis == getRangeAxis()) {
				mappedDatasets.addAll(datasetsMappedToRangeAxis(0));
			}
		}

        // iterate through the datasets that map to the axis and get the union 
        // of the ranges.
        Iterator iterator = mappedDatasets.iterator();
        while (iterator.hasNext()) {
            CategoryDataset d = (CategoryDataset) iterator.next();
            CategoryItemRenderer r = getRendererForDataset(d);
            if (r != null) {
                result = Range.combine(result, r.findRangeBounds(d));
            }
        }
        return result;

    }
