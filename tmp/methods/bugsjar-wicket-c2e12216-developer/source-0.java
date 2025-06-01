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
			formComponent.modelChanging();
			collection.clear();
			if (convertedInput != null)
			{
				collection.addAll(convertedInput);
			}
			formComponent.modelChanged();

			try
			{
				formComponent.getModel().setObject(collection);
			}
			catch (Exception e)
			{
				// ignore this exception because it could be that there
				// is not setter for this collection.
				logger.info("An error occurred while trying to set the new value for the property attached to " + formComponent, e);
			}
		}
	}
