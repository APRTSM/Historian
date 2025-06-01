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
            LOGGER.error("Could not search jar file '" + path + "' for classes matching criteria: " +
                test + " due to an IOException", ioe);
        }
    }
    String extractPath(final URL url) throws UnsupportedEncodingException {
        String urlPath = url.getPath(); // same as getFile but without the Query portion
        //System.out.println(url.getProtocol() + "->" + urlPath);

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
        if (new File(urlPath).exists()) {
            // if URL-encoded file exists, don't decode it
            return urlPath;
        }
        urlPath = URLDecoder.decode(urlPath, Constants.UTF_8.name());
        return urlPath;
    }
    private void loadImplementationsInBundle(final Test test, final String packageName) {
        //Do not remove the cast on the next line as removing it will cause a compile error on Java 7.
        @SuppressWarnings("RedundantCast")
        final BundleWiring wiring = (BundleWiring) FrameworkUtil.getBundle(
                ResolverUtil.class).adapt(BundleWiring.class);
        @SuppressWarnings("unchecked")
        final Collection<String> list = (Collection<String>) wiring.listResources(packageName, "*.class",
            BundleWiring.LISTRESOURCES_RECURSE);
        for (final String name : list) {
            addIfMatching(test, name);
        }
    }
    public void setClassLoader(final ClassLoader classloader) { this.classloader = classloader; }
