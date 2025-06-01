		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			boolean storeLabelText = false;

			// try and find some form of label content...
			String labelText = null;

			// check if the labeled component is a label provider

			if (labeled instanceof ILabelProvider)
			{
				ILabelProvider<String> provider = (ILabelProvider<String>)labeled;
				if (provider.getLabel() != null)
				{
					String text = provider.getLabel().getObject();
					if (!Strings.isEmpty(text))
					{
						labelText = text;
					}
				}
			}

			// check if the labeled component is a form component

			if (labelText == null && labeled instanceof FormComponent)
			{
				String text = ((FormComponent<?>)labeled).getDefaultLabel("wicket:unknown");
				if (!"wicket:unknown".equals(text) && !Strings.isEmpty(text))
				{
					labelText = text;
				}
			}

			// check if wicket:label tag has a message key
			if (labelText == null && openTag.getAttribute("key") != null)
			{
				String text = labeled.getString(openTag.getAttribute("key"));
				if (!Strings.isEmpty(text))
				{
					labelText = text;
					storeLabelText = true;
				}
			}

			// as last resort use the tag body
			if (labelText == null)
			{
				String text = new ResponseBufferZone(RequestCycle.get(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						TextLabel.super.onComponentTagBody(markupStream, openTag);
					}
				}.execute().toString();

				if (!Strings.isEmpty(text))
				{
					labelText = text;
					storeLabelText = true;
				}
			}

			// print the label text
			replaceComponentTagBody(markupStream, openTag, labelText);

			// store the label text in FormComponent's label model so its available to errors
			if (labeled instanceof FormComponent)
			{
				FormComponent<?> fc = (FormComponent<?>)labeled;
				fc.setLabel(Model.of(labelText));
			}
		}
