    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = new LegendItemCollection();
        if (this.plot == null) {
            return result;
        }
        int index = this.plot.getIndexOf(this);
        CategoryDataset dataset = this.plot.getDataset(index);
        int seriesCount = dataset.getRowCount();
        if (plot.getRowRenderingOrder().equals(SortOrder.ASCENDING)) {
            for (int i = 0; i < seriesCount; i++) {
                if (isSeriesVisibleInLegend(i)) {
                    LegendItem item = getLegendItem(index, i);
                    if (item != null) {
                        result.add(item);
                    }
                }
            }
        }
        else {
            for (int i = seriesCount - 1; i >= 0; i--) {
                if (isSeriesVisibleInLegend(i)) {
                    LegendItem item = getLegendItem(index, i);
                    if (item != null) {
                        result.add(item);
                    }
                }
            }
        }
        return result;
    }
    public void setRenderer(int index, CategoryItemRenderer renderer,
                            boolean notify) {

        ValueAxis existing = getRangeAxis();
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // register the new renderer...
        this.renderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        configureDomainAxes();
        configureRangeAxes();

        if (notify) {
            fireChangeEvent();
        }
    }
    public void setDataset(CategoryDataset dataset) {
        if (this.foregroundRangeMarkers != null) {
			Set keys = this.foregroundRangeMarkers.keySet();
			Iterator iterator = keys.iterator();
			while (iterator.hasNext()) {
				Integer key = (Integer) iterator.next();
				clearRangeMarkers(key.intValue());
			}
			this.foregroundRangeMarkers.clear();
		}
		setDataset(0, dataset);
    }
