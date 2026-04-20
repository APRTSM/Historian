        public SystemOutStream() {
        }
        public void write(final byte[] b, final int off, final int len)
            throws IOException {
            System.err.write(b, off, len);
        }
        public void close() {
            // do not close sys out!
        }
        public void flush() {
            System.err.flush();
        }
        public SystemErrStream() {
        }
    private static OutputStream getOutputStream(final boolean follow, final Target target) {
        final String enc = Charset.defaultCharset().name();
        PrintStream printStream = null;
        try {
            // Cannot use a CloseShieldOutputStream here;
            // see org.apache.logging.log4j.core.appender.ConsoleAppenderTest
            // @formatter:off
            printStream = target == Target.SYSTEM_OUT ?
            follow ? new PrintStream(new SystemOutStream(), true, enc) : System.out :
            follow ? new PrintStream(new SystemErrStream(), true, enc) : System.err;
            // @formatter:on
        } catch (final UnsupportedEncodingException ex) { // should never happen
            throw new IllegalStateException("Unsupported default encoding " + enc, ex);
        }
        final PropertiesUtil propsUtil = PropertiesUtil.getProperties();
        if (!propsUtil.getStringProperty("os.name").startsWith("Windows")
                || propsUtil.getBooleanProperty("log4j.skipJansi")) {
            return printStream;
        }
        try {
            // We type the parameter as a wildcard to avoid a hard reference to Jansi.
            final Class<?> clazz = Loader.loadClass(JANSI_CLASS);
            final Constructor<?> constructor = clazz.getConstructor(OutputStream.class);
            OutputStream newInstance = (OutputStream) constructor.newInstance(printStream);
            // LOG4J-965
            return follow ? new CloseShieldOutputStream(newInstance) : newInstance;
        } catch (final ClassNotFoundException cnfe) {
            LOGGER.debug("Jansi is not installed, cannot find {}", JANSI_CLASS);
        } catch (final NoSuchMethodException nsme) {
            LOGGER.warn("{} is missing the proper constructor", JANSI_CLASS);
        } catch (final Exception ex) {
            LOGGER.warn("Unable to instantiate {}", JANSI_CLASS);
        }
        return printStream;
    }
        public void flush() {
            System.out.flush();
        }
        public void write(final byte[] b) throws IOException {
            System.out.write(b);
        }
        public void write(final byte[] b, final int off, final int len)
            throws IOException {
            System.out.write(b, off, len);
        }
        public void write(final int b) throws IOException {
            System.out.write(b);
        }
        public void write(final int b) {
            System.err.write(b);
        }
        public void write(final byte[] b) throws IOException {
            System.err.write(b);
        }
        public void close() {
            // do not close sys err!
        }
