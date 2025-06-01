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
