    public void setDataset(int index, CategoryDataset dataset) {

        int domainAxisCount = this.domainAxes.size();
		CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
        ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(index);
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
