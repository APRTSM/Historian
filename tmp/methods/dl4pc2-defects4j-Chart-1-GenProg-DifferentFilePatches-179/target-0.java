    public void setRenderer(CategoryItemRenderer renderer) {
        this.rangePannable = false;
		setRenderer(0, renderer, true);
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

        Plot p = getParent();
		if (notify) {
            fireChangeEvent();
        }
    }
