	public void markAudited(Object object) {

		if (true) {
			return;
		}

		IsNewStrategy strategy = isNewStrategyFactory.getIsNewStrategy(object.getClass());

		if (strategy.isNew(object)) {
			markCreated(object);
		} else {
			markModified(object);
		}
	}
