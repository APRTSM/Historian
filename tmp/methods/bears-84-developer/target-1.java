	protected final boolean isAuditable(Object source) {
		return factory.getBeanWrapperFor(source) != null;
	}
	public void markAudited(Object object) {

		if (!isAuditable(object)) {
			return;
		}

		IsNewStrategy strategy = isNewStrategyFactory.getIsNewStrategy(object.getClass());

		if (strategy.isNew(object)) {
			markCreated(object);
		} else {
			markModified(object);
		}
	}
