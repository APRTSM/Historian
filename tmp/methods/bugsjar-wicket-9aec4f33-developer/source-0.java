	public SpringBeanLocator(final String beanName, final Class<?> beanType,
		final ISpringContextLocator locator)
	{
		if (locator == null)
		{
			throw new IllegalArgumentException("[locator] argument cannot be null");
		}
		if (beanType == null)
		{
			throw new IllegalArgumentException("[beanType] argument cannot be null");
		}

		beanTypeCache = new WeakReference<Class<?>>(beanType);
		beanTypeName = beanType.getName();
		springContextLocator = locator;
		this.beanName = beanName;
		springContextLocator = locator;
	}
	private static Object lookupSpringBean(final ApplicationContext ctx, final String name,
		final Class<?> clazz)
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
				clazz.getName() + "] not found");
		}
	}
