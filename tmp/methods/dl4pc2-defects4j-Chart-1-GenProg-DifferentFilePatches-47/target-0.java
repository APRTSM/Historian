    public void setDataset(int index, CategoryDataset dataset) {

        int domainAxisCount = this.domainAxes.size();
		CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        datasetChanged(new DatasetChangeEvent(this, getDataset(index),
				new DatasetChangeInfo()));
		this.datasets.set(index, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset,
                new DatasetChangeInfo());
        // TODO: fill in real dataset change info
        datasetChanged(event);

    }
    public void setRenderer(int index, CategoryItemRenderer renderer,
                            boolean notify) {

        // stop listening to the existing renderer...
        CategoryItemRenderer existing
            = (CategoryItemRenderer) this.renderers.get(index);
        // register the new renderer...
        this.renderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        configureDomainAxes();
        configureRangeAxes();

        float lowest = Float.POSITIVE_INFINITY;
		if (notify) {
            fireChangeEvent();
        }
    }
