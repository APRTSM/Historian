	public boolean hasRepositoryFor(Class<?> domainClass) {

		Assert.notNull(domainClass, DOMAIN_TYPE_MUST_NOT_BE_NULL);

		Class<?> userClass = ClassUtils.getUserClass(domainClass);

		return repositoryFactoryInfos.containsKey(userClass);
	}
	public Object getRepositoryFor(Class<?> domainClass) {

		Assert.notNull(domainClass, DOMAIN_TYPE_MUST_NOT_BE_NULL);

		Class<?> userClass = ClassUtils.getUserClass(domainClass);
		String repositoryBeanName = repositoryBeanNames.get(userClass);

		return repositoryBeanName == null || beanFactory == null ? null : beanFactory.getBean(repositoryBeanName);
	}
