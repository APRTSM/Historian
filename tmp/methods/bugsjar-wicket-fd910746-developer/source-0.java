		public void component(final FormComponent<?> formComponent, final IVisit<Void> visit)
		{

			Form<?> form = formComponent.getForm();
			if (!form.isVisibleInHierarchy() || !form.isEnabledInHierarchy())
			{
				// do not validate formComponent or any of formComponent's children
				visit.dontGoDeeper();
				return;
			}

			if (formComponent.isVisibleInHierarchy() && formComponent.isValid() &&
				formComponent.isEnabledInHierarchy())
			{
				validate(formComponent);
			}
			if (formComponent.processChildren() == false)
			{
				visit.dontGoDeeper();
			}
		}
