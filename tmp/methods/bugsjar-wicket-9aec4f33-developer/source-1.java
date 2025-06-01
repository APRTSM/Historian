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
	private String getBeanName(final Field field)
	{
		SpringBean annot = field.getAnnotation(SpringBean.class);
		
		String name;
		boolean required;
		if (annot != null) {
			name = annot.name();
			required = annot.required();
		} else {
			Named named = field.getAnnotation(Named.class);
			name = named != null ? named.value() : "";
			required = false;
		}

		if (Strings.isEmpty(name))
		{
			name = beanNameCache.get(field.getType());
			if (name == null)
			{
				name = getBeanNameOfClass(contextLocator.getSpringContext(), field.getType(), required);

				if (name != null)
				{
					String tmpName = beanNameCache.putIfAbsent(field.getType(), name);
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
			String beanName = getBeanName(field);

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
			if (wrapInProxies)
			{
				target = LazyInitProxyFactory.createProxy(field.getType(), locator);
			}
			else
			{
				target = locator.locateProxyTarget();
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
