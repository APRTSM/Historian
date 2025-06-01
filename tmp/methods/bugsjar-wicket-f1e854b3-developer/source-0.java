		private Map<String, CharSequence> findAndRenderChildWicketTags(
			final MarkupStream markupStream, final ComponentTag openTag)
		{
			Map<String, CharSequence> childTags = new HashMap<String, CharSequence>();

			// get original tag from markup because we modified openTag to always be open
			ComponentTag tag = markupStream.getPreviousTag();

			// if the tag is of form <wicket:message>{foo}</wicket:message> then scan for any
			// child component and save their tag index
			if (!tag.isOpenClose())
			{
				while (markupStream.hasMore() && !markupStream.get().closes(openTag))
				{
					MarkupElement element = markupStream.get();
					// If it a tag like <wicket..> or <span wicket:id="..." >
					if ((element instanceof ComponentTag) && !markupStream.atCloseTag())
					{
						String id = ((ComponentTag)element).getId();

						// Temporarily replace the web response with a String response
						final Response webResponse = getResponse();

						try
						{
							final StringResponse response = new StringResponse();
							getRequestCycle().setResponse(response);

							Component component = getParent().get(id);
							if (component != null)
							{
								component.render();
								markupStream.skipComponent();
							}
							else
							{
								markupStream.next();
							}
							childTags.put(id, response.getBuffer());
						}
						finally
						{
							// Restore the original response
							getRequestCycle().setResponse(webResponse);
						}
					}
					else
					{
						markupStream.next();
					}
				}
			}

			return childTags;
		}
