	public void setInputClassLoader(ClassLoader aClassLoader) {
		if (aClassLoader instanceof URLClassLoader) {
			final URL[] urls = ((URLClassLoader) aClassLoader).getURLs();
			if (urls != null && urls.length > 0) {
				List<String> classpath = new ArrayList<>();
				for (URL url : urls) {
					classpath.add(url.toString());
				}
				setSourceClasspath(classpath.toArray(new String[0]));
			}
			return;
		}
		this.classloader = aClassLoader;
	}
