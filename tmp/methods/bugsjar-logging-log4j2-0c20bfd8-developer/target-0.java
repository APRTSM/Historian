    public void findInPackage(final Test test, String packageName) {
        packageName = packageName.replace('.', '/');
        final ClassLoader loader = getClassLoader();
        Enumeration<URL> urls;

        try {
            urls = loader.getResources(packageName);
        } catch (final IOException ioe) {
            LOGGER.warn("Could not read package: " + packageName, ioe);
            return;
        }

        while (urls.hasMoreElements()) {
            try {
                final URL url = urls.nextElement();
                final String urlPath = extractPath(url);

                LOGGER.info("Scanning for classes in [" + urlPath + "] matching criteria: " + test);
                // Check for a jar in a war in JBoss
                if (VFSZIP.equals(url.getProtocol())) {
                    final String path = urlPath.substring(0, urlPath.length() - packageName.length() - 2);
                    final URL newURL = new URL(url.getProtocol(), url.getHost(), path);
                    @SuppressWarnings("resource")
                    final JarInputStream stream = new JarInputStream(newURL.openStream());
                    try {
                        loadImplementationsInJar(test, packageName, path, stream);
                    } finally {
                        close(stream, newURL);
                    }
                } else if (BUNDLE_RESOURCE.equals(url.getProtocol())) {
                    loadImplementationsInBundle(test, packageName);
                } else {
                    final File file = new File(urlPath);
                    if (file.isDirectory()) {
                        loadImplementationsInDirectory(test, packageName, file);
                    } else {
                        loadImplementationsInJar(test, packageName, file);
                    }
                }
            } catch (final IOException ioe) {
                LOGGER.warn("could not read entries", ioe);
            } catch (URISyntaxException e) {
                LOGGER.warn("could not read entries", e);
            }
        }
    }
    private void loadImplementationsInBundle(final Test test, final String packageName) {
        // Do not remove the cast on the next line as removing it will cause a compile error on Java 7.
        @SuppressWarnings("RedundantCast")
        final BundleWiring wiring = (BundleWiring) FrameworkUtil.getBundle(ResolverUtil.class)
                .adapt(BundleWiring.class);
        @SuppressWarnings("unchecked")
        final Collection<String> list = (Collection<String>) wiring.listResources(packageName, "*.class",
                BundleWiring.LISTRESOURCES_RECURSE);
        for (final String name : list) {
            addIfMatching(test, name);
        }
    }
    private void loadImplementationsInJar(final Test test, final String parent, final String path,
            final JarInputStream stream) {

        try {
            JarEntry entry;

            while ((entry = stream.getNextJarEntry()) != null) {
                final String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(parent) && isTestApplicable(test, name)) {
                    addIfMatching(test, name);
                }
            }
        } catch (final IOException ioe) {
            LOGGER.error("Could not search jar file '" + path + "' for classes matching criteria: " + test
                    + " due to an IOException", ioe);
        }
    }
    String extractPath(final URL url) throws UnsupportedEncodingException, URISyntaxException {
        String urlPath = url.getPath(); // same as getFile but without the Query portion
        // System.out.println(url.getProtocol() + "->" + urlPath);

        // I would be surprised if URL.getPath() ever starts with "jar:" but no harm in checking
        if (urlPath.startsWith("jar:")) {
            urlPath = urlPath.substring(4);
        }
        // For jar: URLs, the path part starts with "file:"
        if (urlPath.startsWith("file:")) {
            urlPath = urlPath.substring(5);
        }
        // If it was in a JAR, grab the path to the jar
        if (urlPath.indexOf('!') > 0) {
            urlPath = urlPath.substring(0, urlPath.indexOf('!'));
        }

        // LOG4J2-445
        // Finally, decide whether to URL-decode the file name or not...
        final String protocol = url.getProtocol();
        final List<String> neverDecode = Arrays.asList(VFSZIP, BUNDLE_RESOURCE);
        if (neverDecode.contains(protocol)) {
            return urlPath;
        }
        final String cleanPath = new URI(urlPath).getPath();
        if (new File(cleanPath).exists()) {
            // if URL-encoded file exists, don't decode it
            return cleanPath;
        }
        urlPath = URLDecoder.decode(urlPath, Constants.UTF_8.name());
        return urlPath;
    }
    public void setClassLoader(final ClassLoader classloader) {
        this.classloader = classloader;
    }
