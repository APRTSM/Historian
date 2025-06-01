    public void setDataset(int index, CategoryDataset dataset) {

        CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
        ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(index);
        this.datasets.set(index, dataset);
        if (dataset != null) {
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset,
                new DatasetChangeInfo());
        // TODO: fill in real dataset change info
        datasetChanged(event);

    }
    public void setRenderer(int index, CategoryItemRenderer renderer,
                            boolean notify) {

        String label = null;
		// stop listening to the existing renderer...
        CategoryItemRenderer existing
            = (CategoryItemRenderer) this.renderers.get(index);
        // register the new renderer...
        this.renderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            if (index < 0) {
				throw new IllegalArgumentException("Requires 'index' >= 0.");
			}
			renderer.addChangeListener(this);
        }

        configureRangeAxes();

        if (notify) {
            fireChangeEvent();
        }
    }
