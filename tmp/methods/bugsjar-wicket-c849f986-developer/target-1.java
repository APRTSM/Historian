	protected void onRender()
	{
		// clear multipart hint, it will be set if necessary by the visitor
		this.multiPart &= ~MULTIPART_HINT;

		// Force multi-part on if any child form component is multi-part
		visitFormComponents(new FormComponent.AbstractVisitor()
		{
			@Override
			public void onFormComponent(FormComponent<?> formComponent)
			{
				if (formComponent.isVisible() && formComponent.isMultiPart())
				{
					multiPart |= MULTIPART_HINT;
				}
			}
		});

		super.onRender();
	}
	public void setMultiPart(boolean multiPart)
	{
		if (multiPart)
		{
			this.multiPart |= MULTIPART_HARD;
		}
		else
		{
			this.multiPart &= ~MULTIPART_HARD;
		}
	}
	private boolean isMultiPart()
	{
		if (multiPart != 0)
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
					if (form.multiPart != 0)
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
	public boolean isMultiPart()
	{
		return true;
	}
