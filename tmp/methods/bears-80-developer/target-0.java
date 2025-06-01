	public void setInputClassLoader(ClassLoader aClassLoader) {
		if (aClassLoader instanceof URLClassLoader) {
			final URL[] urls = ((URLClassLoader) aClassLoader).getURLs();
			if (urls != null && urls.length > 0) {
				// Check that the URLs are only file URLs
				boolean onlyFileURLs = true;
				for (URL url : urls) {
					if (!url.getProtocol().equals("file")) {
						onlyFileURLs = false;
					}
				}
				if (onlyFileURLs) {
					List<String> classpath = new ArrayList<>();
					for (URL url : urls) {
						classpath.add(url.getPath());
					}
					setSourceClasspath(classpath.toArray(new String[0]));
				} else {
					throw new SpoonException("Spoon does not support a URLClassLoader containing other resources than local file.");
				}
			}
			return;
		}
		this.classloader = aClassLoader;
	}
