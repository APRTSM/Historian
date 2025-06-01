	public static <S> void updateCollectionModel(FormComponent<Collection<S>> formComponent)
	{
		Collection<S> convertedInput = formComponent.getConvertedInput();
		if (convertedInput == null) {
			convertedInput = Collections.emptyList();
		}

		Collection<S> collection = formComponent.getModelObject();
		if (collection == null)
		{
			collection = new ArrayList<>(convertedInput);
			formComponent.setModelObject(collection);
		}
		else
		{
			boolean modified = false;

			formComponent.modelChanging();

			try
			{
				collection.clear();
				collection.addAll(convertedInput);
				modified = true;
			}
			catch (UnsupportedOperationException unmodifiable)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("An error occurred while trying to modify the collection attached to "
							+ formComponent, unmodifiable);
				}
				collection = new ArrayList<>(convertedInput); 
			}
			
			try
			{
				formComponent.getModel().setObject(collection);
			}
			catch (Exception noSetter)
			{
				if (!modified)
				{
					throw new WicketRuntimeException("An error occurred while trying to set the collection attached to "
							+ formComponent, noSetter);
				}
				else if (logger.isDebugEnabled())
				{
					logger.debug("An error occurred while trying to set the collection attached to "
							+ formComponent, noSetter);
				}
			}
			
			formComponent.modelChanged();
		}
	}
