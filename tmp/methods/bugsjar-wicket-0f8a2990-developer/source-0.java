	private void ensureAllChildrenPresent(final MarkupContainer container,
		final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, enclosureOpenTag);
		while (it.hasNext())
		{
			final ComponentTag tag = it.next();

			Component child = container.get(tag.getId());
			if (child == null)
			{
				// component does not yet exist in the container, attempt to resolve it using
				// resolvers
				final int tagIndex = it.getCurrentIndex();

				// because the resolvers can auto-add and therefore immediately render the component
				// we have to buffer the output since we do not yet know the visibility of the
				// enclosure
				CharSequence buffer = new ResponseBufferZone(getRequestCycle(), markupStream)
				{
					@Override
					protected void executeInsideBufferedZone()
					{
						markupStream.setCurrentIndex(tagIndex);
						ComponentResolvers.resolve(container, markupStream, tag);
					}
				}.execute();

				child = container.get(tag.getId());
				checkChildComponent(child);

				if (buffer.length() > 0)
				{
					// we have already rendered this child component, insert a stub component that
					// will dump the markup during the normal render process if the enclosure is
					// visible
					final Component stub = new AutoMarkupLabel(child.getId(), buffer);
					container.replace(stub); // ok here because we are replacing auto with auto
				}
			}
		}
		it.rewind();
	}
	private void checkChildComponent(Component controller)
	{
		if (controller == null)
		{
			throw new WicketRuntimeException("Could not find child with id: " + childId +
				" in the wicket:enclosure");
		}
		else if (controller == this)
		{
			throw new WicketRuntimeException(
				"Programming error: childComponent == enclose component; endless loop");
		}
	}
	private void applyEnclosureVisibilityToChildren(final MarkupContainer container,
		final MarkupStream markupStream, ComponentTag enclosureOpenTag)
	{
		DirectChildTagIterator it = new DirectChildTagIterator(markupStream, enclosureOpenTag);
		while (it.hasNext())
		{
			final ComponentTag tag = it.next();
			final Component child = container.get(tag.getId());
			// record original visiblity allowed value, will restore later
			changes.put(child, child.isVisibilityAllowed());
			child.setVisibilityAllowed(isVisible());
		}
		it.rewind();
	}
