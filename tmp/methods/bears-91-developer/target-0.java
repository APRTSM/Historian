	private static <T> T getUniqueBean(Class<T> type, ApplicationContext context) {

		try {
			return context.getBean(type);
		} catch (NoSuchBeanDefinitionException o_O) {
			return null;
		}
	}
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		if (ClassUtils.isPresent("com.jayway.jsonpath.DocumentContext", context.getClassLoader())
				&& ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", context.getClassLoader())) {

			ObjectMapper mapper = getUniqueBean(ObjectMapper.class, context);
			mapper = mapper == null ? new ObjectMapper() : mapper;

			ProjectingJackson2HttpMessageConverter converter = new ProjectingJackson2HttpMessageConverter(mapper);
			converter.setBeanClassLoader(context.getClassLoader());
			converter.setBeanFactory(context);

			converters.add(0, converter);
		}

		if (ClassUtils.isPresent("org.xmlbeam.XBProjector", context.getClassLoader())) {
			converters.add(0, new XmlBeamHttpMessageConverter());
		}
	}
