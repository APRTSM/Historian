	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		if (ClassUtils.isPresent("com.jayway.jsonpath.DocumentContext", context.getClassLoader())
				&& ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", context.getClassLoader())) {

			ProjectingJackson2HttpMessageConverter converter = new ProjectingJackson2HttpMessageConverter(new ObjectMapper());
			converter.setBeanClassLoader(context.getClassLoader());
			converter.setBeanFactory(context);

			converters.add(0, converter);
		}

		if (ClassUtils.isPresent("org.xmlbeam.XBProjector", context.getClassLoader())) {
			converters.add(0, new XmlBeamHttpMessageConverter());
		}
	}
