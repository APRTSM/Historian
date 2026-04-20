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
