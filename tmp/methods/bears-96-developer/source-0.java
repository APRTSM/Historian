	public Object getRepositoryFor(Class<?> domainClass) {

		Assert.notNull(domainClass, DOMAIN_TYPE_MUST_NOT_BE_NULL);

		String repositoryBeanName = repositoryBeanNames.get(domainClass);
		return repositoryBeanName == null || beanFactory == null ? null : beanFactory.getBean(repositoryBeanName);
	}
	public boolean hasRepositoryFor(Class<?> domainClass) {

		Assert.notNull(domainClass, DOMAIN_TYPE_MUST_NOT_BE_NULL);

		return repositoryFactoryInfos.containsKey(domainClass);
	}
