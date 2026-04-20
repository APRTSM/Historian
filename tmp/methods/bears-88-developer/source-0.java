	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {

		AnnotatedBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(getRepositoryInterfaceFrom(definition));
		return DELEGATE.generateBeanName(beanDefinition, registry);
	}
