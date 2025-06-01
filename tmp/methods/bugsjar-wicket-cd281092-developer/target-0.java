		public String getMessage(String key)
		{
			final FormComponent<T> formComponent = FormComponent.this;

			// Use the following log4j config for detailed logging on the property resolution
			// process
			// log4j.logger.org.apache.wicket.resource.loader=DEBUG
			// log4j.logger.org.apache.wicket.Localizer=DEBUG

			final Localizer localizer = formComponent.getLocalizer();

			// retrieve prefix that will be used to construct message keys
			String prefix = formComponent.getValidatorKeyPrefix();
			String message = null;

			// first try the full form of key [form-component-id].[key]
			String resource = getId() + "." + prefix(prefix, key);
			message = getString(localizer, resource, formComponent);

			// if not found, try a more general form (without prefix)
			// [form-component-id].[prefix].[key]
			if (Strings.isEmpty(message) && Strings.isEmpty(prefix))
			{
				resource = getId() + "." + key;
				message = getString(localizer, resource, formComponent);
			}

			// If not found try a more general form [prefix].[key]
			if (Strings.isEmpty(message))
			{
				resource = prefix(prefix, key);
				message = getString(localizer, key, formComponent);
			}

			// If not found try the most general form [key]
			if (Strings.isEmpty(message) && Strings.isEmpty(prefix))
			{
				// Try a variation of the resource key
				message = getString(localizer, key, formComponent);
			}

			// convert empty string to null in case our default value of "" was
			// returned from localizer
			if (Strings.isEmpty(message))
			{
				message = null;
			}
			return message;
		}
