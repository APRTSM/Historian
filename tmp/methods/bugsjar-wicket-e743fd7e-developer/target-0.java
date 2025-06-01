		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{

			// try and find some form of label content...
			IModel<String> labelModel = findLabelContent(markupStream, openTag);
			// print the label text
			replaceComponentTagBody(markupStream, openTag,
				labelModel != null ? labelModel.getObject() : "");

			// store the label text in FormComponent's label model so its available to errors
			if (labelModel != null)
			{
				if (labeled instanceof FormComponent)
				{
					FormComponent<?> fc = (FormComponent<?>)labeled;
					fc.setLabel(labelModel);
				}
				else
				{
					// if we can't hand off the labelmodel to a component, we have to detach it
					labelModel.detach();
				}
			}
		}
		private IModel<String> findLabelContent(final MarkupStream markupStream,
			final ComponentTag tag)
		{
			if (labeled instanceof ILabelProvider)
			{
				// check if the labeled component is a label provider
				ILabelProvider<String> provider = (ILabelProvider<String>)labeled;
				if (provider.getLabel() != null)
				{
					if (!Strings.isEmpty(provider.getLabel().getObject()))

					{
						return provider.getLabel();
					}
				}
			}

			// check if the labeled component is a form component
			if (labeled instanceof FormComponent)
			{
				final FormComponent<?> formComponent = (FormComponent<?>)labeled;
				String text = formComponent.getDefaultLabel("wicket:unknown");
				if (!"wicket:unknown".equals(text) && !Strings.isEmpty(text))
				{
					return new LoadableDetachableModel<String>()
					{
						@Override
						protected String load()
						{
							return formComponent.getDefaultLabel("wicket:unknown");
						}
					};
				}
			}

			// check if wicket:label tag has a message key
			{
				String resourceKey = tag.getAttribute("key");
				if (resourceKey != null)
				{
					String text = labeled.getString(resourceKey);
					if (!Strings.isEmpty(text))
					{
						return new StringResourceModel(resourceKey, labeled, null);
					}
				}
			}

			// as last resort use the tag body
			{
				String text = new ResponseBufferZone(RequestCycle.get(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						TextLabel.super.onComponentTagBody(markupStream, tag);
					}
				}.execute().toString();

				if (!Strings.isEmpty(text))
				{
					return Model.of(text);
				}
			}

			return null;
		}
