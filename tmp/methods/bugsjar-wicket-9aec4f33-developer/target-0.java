	private Object lookupSpringBean(ApplicationContext ctx, String name, Class<?> clazz)
	{
		try
		{
			if (name == null)
			{
				return ctx.getBean(clazz);
			}
			else
			{
				return ctx.getBean(name, clazz);
			}
		}
		catch (NoSuchBeanDefinitionException e)
		{
			throw new IllegalStateException("bean with name [" + name + "] and class [" +
				clazz.getName() + "] not found", e);
		}
	}
	public SpringBeanLocator(final String beanName, final Class<?> beanType,
		final ISpringContextLocator locator)
	{
		Args.notNull(locator, "locator");
		Args.notNull(beanType, "beanType");

		beanTypeCache = new WeakReference<Class<?>>(beanType);
		beanTypeName = beanType.getName();
		springContextLocator = locator;
		this.beanName = beanName;
		springContextLocator = locator;
	}
