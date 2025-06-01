		public T getObject()
		{
			return getParentModel().getObject();
		}
	protected final Component getLabel()
	{
		if (label == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		return label;
	}
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// lazily add label and editor
		if (editor == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		// obsolete with WICKET-1919
		// label.setEnabled(isEnabledInHierarchy());
	}
		public Class<T> getObjectClass()
		{
			if (getParentModel() instanceof IObjectClassAwareModel)
			{
				return ((IObjectClassAwareModel)getParentModel()).getObjectClass();
			}
			else
			{
				return null;
			}
		}
		public void setObject(final T object)
		{
			getParentModel().setObject(object);
		}
		public void detach()
		{
			getParentModel().detach();

		}
	protected final FormComponent<T> getEditor()
	{
		if (editor == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		return editor;
	}
