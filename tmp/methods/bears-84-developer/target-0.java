	protected final boolean isAuditable(Object source) {
		return factory.getBeanWrapperFor(source) != null;
	}
