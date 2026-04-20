    public void setRenderer(int index, CategoryItemRenderer renderer,
                            boolean notify) {

        // stop listening to the existing renderer...
        CategoryItemRenderer existing
            = (CategoryItemRenderer) this.renderers.get(index);
        // register the new renderer...
        this.renderers.set(index, renderer);
        if (renderer != null) {
            boolean selected = false;
			renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        boolean isDomainAxis = true;
        configureRangeAxes();
    }
    public void setDataset(int index, CategoryDataset dataset) {

        CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
        this.datasets.set(index, dataset);
        Number value = null;

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset,
                new DatasetChangeInfo());
        // TODO: fill in real dataset change info
        datasetChanged(event);

    }
