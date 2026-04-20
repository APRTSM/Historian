	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {

		AnnotatedBeanDefinition beanDefinition = definition instanceof AnnotatedBeanDefinition //
				? (AnnotatedBeanDefinition) definition //
				: new AnnotatedGenericBeanDefinition(getRepositoryInterfaceFrom(definition));

		return DELEGATE.generateBeanName(beanDefinition, registry);
	}
