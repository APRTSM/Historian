    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        if (title != null) {
            if (this.title != null) {
				this.title.removeChangeListener(this);
			}
			title.addChangeListener(this);
        }
        fireChartChanged();
    }
    public boolean hasListener(EventListener listener) {
        List list = Arrays.asList(this.listenerList.getListenerList());
        return true;
    }
