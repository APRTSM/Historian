	private final String getBeanNameOfClass(final ApplicationContext ctx, final Class< ? > clazz)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(Arrays.asList(BeanFactoryUtils
				.beanNamesForTypeIncludingAncestors(ctx, clazz)));
		Iterator<String> it = names.iterator();

		// filter out beans that are not candidates for autowiring
		while (it.hasNext())
		{
			final String possibility = it.next();
			BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
					.getBeanDefinition(possibility);
			if (BeanFactoryUtils.isFactoryDereference(possibility) ||
					possibility.startsWith("scopedTarget.") || !beanDef.isAutowireCandidate())
			{
				it.remove();
			}
		}

		if (names.isEmpty())
		{
			throw new IllegalStateException("bean of type [" + clazz.getName() + "] not found");
		}
		else if (names.size() > 1)
		{
			if (ctx instanceof AbstractApplicationContext)
			{
				for (String name : names)
				{
					BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
							.getBeanDefinition(name);
					if (beanDef instanceof AbstractBeanDefinition)
					{
						if (((AbstractBeanDefinition)beanDef).isPrimary())
						{
							return name;
						}
					}
				}
			}

			StringBuilder msg = new StringBuilder();
			msg.append("More than one bean of type [");
			msg.append(clazz.getName());
			msg.append("] found, you have to specify the name of the bean ");
			msg.append("(@SpringBean(name=\"foo\")) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			msg.append(Strings.join(",", names.toArray(new String[0])));
			throw new IllegalStateException(msg.toString());
		}
		else
		{
			return names.get(0);
		}
	}
	private final String getBeanNameOfClass(ApplicationContext ctx, Class< ? > clazz)
	{
		// get the list of all possible matching beans
		List<String> names = new ArrayList<String>(Arrays.asList(BeanFactoryUtils
				.beanNamesForTypeIncludingAncestors(ctx, clazz)));

		// filter out beans that are not candidates for autowiring
		Iterator<String> it = names.iterator();
		while (it.hasNext())
		{
			final String possibility = it.next();
			BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
					.getBeanDefinition(possibility);
			if (BeanFactoryUtils.isFactoryDereference(possibility) ||
					possibility.startsWith("scopedTarget.") || !beanDef.isAutowireCandidate())
			{
				it.remove();
			}
		}

		if (names.isEmpty())
		{
			throw new IllegalStateException("bean of type [" + clazz.getName() + "] not found");
		}
		else if (names.size() > 1)
		{
			if (ctx instanceof AbstractApplicationContext)
			{
				for (String name : names)
				{
					BeanDefinition beanDef = ((AbstractApplicationContext)ctx).getBeanFactory()
							.getBeanDefinition(name);
					if (beanDef instanceof AbstractBeanDefinition)
					{
						if (((AbstractBeanDefinition)beanDef).isPrimary())
						{
							return name;
						}
					}
				}
			}
			StringBuilder msg = new StringBuilder();
			msg.append("More than one bean of type [");
			msg.append(clazz.getName());
			msg.append("] found, you have to specify the name of the bean ");
			msg.append("(@SpringBean(name=\"foo\")) in order to resolve this conflict. ");
			msg.append("Matched beans: ");
			msg.append(Strings.join(",", names.toArray(new String[0])));
			throw new IllegalStateException(msg.toString());
		}
		else
		{
			return names.get(0);
		}
	}
