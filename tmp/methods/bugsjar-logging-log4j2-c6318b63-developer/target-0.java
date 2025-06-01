    public String lookup(final LogEvent event, final String key) {
        if (key == null) {
            return null;
        }
        final String jndiName = convertJndiName(key);
        final JndiManager jndiManager = JndiManager.getDefaultManager();
        try {
            return String.valueOf(jndiManager.lookup(jndiName));
        } catch (final NamingException e) {
            LOGGER.warn(LOOKUP, "Error looking up JNDI resource [{}].", jndiName, e);
            return null;
        } finally {
            jndiManager.release();
        }
    }
