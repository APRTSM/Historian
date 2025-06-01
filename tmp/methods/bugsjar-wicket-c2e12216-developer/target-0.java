	public static <S> void updateCollectionModel(FormComponent<Collection<S>> formComponent)
	{
		Collection<S> convertedInput = formComponent.getConvertedInput();

		Collection<S> collection = formComponent.getModelObject();
		if (collection == null)
		{
			collection = new ArrayList<>(convertedInput);
			formComponent.setDefaultModelObject(collection);
		}
		else
		{
			Exception failure;

			formComponent.modelChanging();

			try {
				collection.clear();
				if (convertedInput != null)
				{
					collection.addAll(convertedInput);
				}
				failure = null;
			} catch (UnsupportedOperationException unmodifiable) {
				logger.debug("An error occurred while trying to change the collection attached to " + formComponent, unmodifiable);

				failure = unmodifiable;
				collection = new ArrayList<>(convertedInput);
			}

			try
			{
				formComponent.getModel().setObject(collection);
				failure = null;
			}
			catch (Exception noSetter)
			{
				logger.debug("An error occurred while trying to set the collection attached to " + formComponent, noSetter);

				if (failure != null) {
					failure = noSetter;
				}
			}

			if (failure == null) {
				formComponent.modelChanged();
			} else {
				throw new WicketRuntimeException("Unable to update the collection attached to " + formComponent);
			}
		}
	}
