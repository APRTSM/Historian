    private static OutputStream getOutputStream(final boolean follow, final Target target) {
        final String enc = Charset.defaultCharset().name();
        PrintStream printStream = null;
        try {
            printStream = target == Target.SYSTEM_OUT ?
            follow ? new PrintStream(new CloseShieldOutputStream(System.out), true, enc) : System.out :
            follow ? new PrintStream(new CloseShieldOutputStream(System.err), true, enc) : System.err;
        } catch (final UnsupportedEncodingException ex) { // should never happen
            throw new IllegalStateException("Unsupported default encoding " + enc, ex);
        }
        final PropertiesUtil propsUtil = PropertiesUtil.getProperties();
        if (!propsUtil.getStringProperty("os.name").startsWith("Windows") ||
            propsUtil.getBooleanProperty("log4j.skipJansi")) {
            return printStream;
        }
        try {
            // We type the parameter as a wildcard to avoid a hard reference to Jansi.
            final Class<?> clazz = Loader.loadClass(JANSI_CLASS);
            final Constructor<?> constructor = clazz.getConstructor(OutputStream.class);
            // LOG4J-965
            return new CloseShieldOutputStream((OutputStream) constructor.newInstance(printStream));
        } catch (final ClassNotFoundException cnfe) {
            LOGGER.debug("Jansi is not installed, cannot find {}", JANSI_CLASS);
        } catch (final NoSuchMethodException nsme) {
            LOGGER.warn("{} is missing the proper constructor", JANSI_CLASS);
        } catch (final Exception ex) {
            LOGGER.warn("Unable to instantiate {}", JANSI_CLASS);
        }
        return printStream;
    }
