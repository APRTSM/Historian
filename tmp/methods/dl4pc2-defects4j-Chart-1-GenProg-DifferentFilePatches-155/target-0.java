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

        configureDomainAxes();
        configureRangeAxes();
    }
