    public DefaultLevelConverter() {
        JDK_TO_LOG4J.put(java.util.logging.Level.OFF, Level.OFF);
        JDK_TO_LOG4J.put(java.util.logging.Level.FINEST, LevelTranslator.FINEST);
        JDK_TO_LOG4J.put(java.util.logging.Level.FINER, Level.TRACE);
        JDK_TO_LOG4J.put(java.util.logging.Level.FINE, Level.DEBUG);
        JDK_TO_LOG4J.put(java.util.logging.Level.CONFIG, LevelTranslator.CONFIG);
        JDK_TO_LOG4J.put(java.util.logging.Level.INFO, Level.INFO);
        JDK_TO_LOG4J.put(java.util.logging.Level.WARNING, Level.WARN);
        JDK_TO_LOG4J.put(java.util.logging.Level.SEVERE, Level.ERROR);
        JDK_TO_LOG4J.put(java.util.logging.Level.ALL, Level.ALL);
        LOG4J_TO_JDK.put(Level.OFF, java.util.logging.Level.OFF);
        LOG4J_TO_JDK.put(LevelTranslator.FINEST, java.util.logging.Level.FINEST);
        LOG4J_TO_JDK.put(Level.TRACE, java.util.logging.Level.FINER);
        LOG4J_TO_JDK.put(Level.DEBUG, java.util.logging.Level.FINE);
        LOG4J_TO_JDK.put(LevelTranslator.CONFIG, java.util.logging.Level.CONFIG);
        LOG4J_TO_JDK.put(Level.INFO, java.util.logging.Level.INFO);
        LOG4J_TO_JDK.put(Level.WARN, java.util.logging.Level.WARNING);
        LOG4J_TO_JDK.put(Level.ERROR, java.util.logging.Level.SEVERE);
        LOG4J_TO_JDK.put(Level.ALL, java.util.logging.Level.ALL);
    }
