	protected final Component getLabel()
	{
		if (label == null)
		{
			initLabelAndEditor(getDelegatingParentModel());
		}
		return label;
	}
	protected final FormComponent<T> getEditor()
	{
		if (editor == null)
		{
			initLabelAndEditor(getDelegatingParentModel());
		}
		return editor;
	}
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// lazily add label and editor
		if (editor == null)
		{
			initLabelAndEditor(getDelegatingParentModel());
		}
		// obsolete with WICKET-1919
		// label.setEnabled(isEnabledInHierarchy());
	}
	private IModel<T> getDelegatingParentModel()
	{
		return new IModel<T>()
		{
			private static final long serialVersionUID = 1L;

			public T getObject()
			{
				return getParentModel().getObject();
			}

			public void setObject(final T object)
			{
				getParentModel().setObject(object);
			}

			public void detach()
			{
				getParentModel().detach();
			}
		};
	}
