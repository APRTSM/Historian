	protected void onRender()
	{
		// Force multi-part on if any child form component is multi-part
		visitFormComponents(new FormComponent.AbstractVisitor()
		{
			@Override
			public void onFormComponent(FormComponent<?> formComponent)
			{
				if (formComponent.isVisible() && formComponent.isMultiPart())
				{
					setMultiPart(true);
				}
			}
		});

		super.onRender();
	}
	public void setMultiPart(boolean multiPart)
	{
		this.multiPart = multiPart;
	}
	private boolean isMultiPart()
	{
		if (multiPart)
		{
			return true;
		}
		else
		{
			final boolean[] anyEmbeddedMultipart = new boolean[] { false };
			visitChildren(Form.class, new IVisitor<Form<?>>()
			{

				public Object component(Form<?> form)
				{
					if (form.multiPart)
					{
						anyEmbeddedMultipart[0] = true;
						return STOP_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL;
					}
				}

			});
			return anyEmbeddedMultipart[0];
		}
	}
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		// auto toggle form's multipart property
		Form<?> form = findParent(Form.class);
		if (form == null)
		{
			// woops
			throw new IllegalStateException("Component " + getClass().getName() + " must have a " +
				Form.class.getName() + " component above in the hierarchy");
		}
		form.setMultiPart(true);
	}
