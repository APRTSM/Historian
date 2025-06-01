	public void markAudited(Object object) {

		if (object == null) {
			return;
		}

		IsNewStrategy strategy = isNewStrategyFactory.getIsNewStrategy(object.getClass());

		if (strategy.isNew(object)) {
			markCreated(object);
		} else {
			markModified(object);
		}
	}
