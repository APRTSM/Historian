	final void setMarkupId(Component comp)
	{
		Args.notNull(comp, "comp");

		generatedMarkupId = comp.generatedMarkupId;
		setMetaData(MARKUP_ID_KEY, comp.getMetaData(MARKUP_ID_KEY));
		setOutputMarkupId(comp.getOutputMarkupId());
		return;
	}
	public final MarkupContainer replace(final Component child)
	{
		checkHierarchyChange(child);

		if (child == null)
		{
			throw new IllegalArgumentException("argument child must be not null");
		}

		if (log.isDebugEnabled())
		{
			log.debug("Replacing " + child.getId() + " in " + this);
		}

		if (child.getParent() != this)
		{
			// Add to map
			final Component replaced = put(child);

			// Look up to make sure it was already in the map
			if (replaced == null)
			{
				throw new WicketRuntimeException(
					exceptionMessage("Cannot replace a component which has not been added: id='" +
						child.getId() + "', component=" + child));
			}

			// first remove the component.
			removedComponent(replaced);

			// then add the other one.
			addedComponent(child);

			// The generated markup id remains the same
			child.setMarkupId(replaced);
		}

		return this;
	}
	protected void onComponentTag(final ComponentTag tag)
	{
		if (tag.isOpenClose())
		{
			wasOpenCloseTag = true;

			// Convert <span wicket:id="myPanel" /> into
			// <span wicket:id="myPanel">...</span>
			tag.setType(XmlTag.OPEN);
		}

// IMarkupFragment markup = getMarkup(null);
// ComponentTag panelTag = (ComponentTag)markup.get(0);
// for (String key : panelTag.getAttributes().keySet())
// {
// tag.append(key, panelTag.getAttribute(key), ", ");
// }
		super.onComponentTag(tag);
	}
