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
	private String getBeanName(final Field field, String name, boolean required)
	{

		if (Strings.isEmpty(name))
		{
			Class<?> fieldType = field.getType();
			name = beanNameCache.get(fieldType);
			if (name == null)
			{
				name = getBeanNameOfClass(contextLocator.getSpringContext(), fieldType, required);

				if (name != null)
				{
					String tmpName = beanNameCache.putIfAbsent(fieldType, name);
					if (tmpName != null)
					{
						name = tmpName;
					}
				}
			}
		}

		return name;
	}
	public Object getFieldValue(final Field field, final Object fieldOwner)
	{
		if (supportsField(field))
		{
			SpringBean annot = field.getAnnotation(SpringBean.class);

			String name;
			boolean required;
			if (annot != null)
			{
				name = annot.name();
				required = annot.required();
			}
			else
			{
				Named named = field.getAnnotation(Named.class);
				name = named != null ? named.value() : "";
				required = false;
			}

			String beanName = getBeanName(field, name, required);

			if (beanName == null)
			{
				return null;
			}

			SpringBeanLocator locator = new SpringBeanLocator(beanName, field.getType(),
				contextLocator);

			// only check the cache if the bean is a singleton
			Object cachedValue = cache.get(locator);
			if (cachedValue != null)
			{
				return cachedValue;
			}

			Object target;
			try
			{
				// check whether there is a bean with the provided properties
				target = locator.locateProxyTarget();
			}
			catch (IllegalStateException isx)
			{
				if (required)
				{
					throw isx;
				}
				else
				{
					return null;
				}
			}

			if (wrapInProxies)
			{
				target = LazyInitProxyFactory.createProxy(field.getType(), locator);
			}

			// only put the proxy into the cache if the bean is a singleton
			if (locator.isSingletonBean())
			{
				Object tmpTarget = cache.putIfAbsent(locator, target);
				if (tmpTarget != null)
				{
					target = tmpTarget;
				}
			}
			return target;
		}
		return null;
	}
