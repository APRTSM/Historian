	private boolean anyFormComponentError()
	{
		final boolean[] error = new boolean[] { false };

		final IVisitor<Component> visitor = new IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				if (component.hasErrorMessage())
				{
					error[0] = true;
					return Component.IVisitor.STOP_TRAVERSAL;
				}

				// Traverse all children
				return Component.IVisitor.CONTINUE_TRAVERSAL;
			}
		};

		visitChildren(FormComponent.class, new IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				return visitor.component(component);
			}
		});

		if (!error[0])
		{
			if (getParent() instanceof Border)
			{
				MarkupContainer border = getParent();
				Iterator<? extends Component> iter = border.iterator();
				while (iter.hasNext())
				{
					Component child = iter.next();
					if ((child != this) && (child instanceof FormComponent))
					{
						visitor.component(child);
						if (error[0])
						{
							break;
						}
					}
				}
			}
		}

		return error[0];
	}
