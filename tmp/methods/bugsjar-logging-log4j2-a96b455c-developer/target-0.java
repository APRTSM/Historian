    private Level addCustomJulLevel(java.util.logging.Level customJavaLevel) {
        long prevDist = Long.MAX_VALUE;
        java.util.logging.Level prevLevel = null;
        for (java.util.logging.Level mappedJavaLevel : sortedJulLevels) {
            long distance = distance(customJavaLevel, mappedJavaLevel);
            if (distance > prevDist) {
                return mapCustomJulLevel(customJavaLevel, prevLevel);
            }
            prevDist = distance;
            prevLevel = mappedJavaLevel;
        }
        return mapCustomJulLevel(customJavaLevel, prevLevel);
    }
    public DefaultLevelConverter() {
        // Map JUL to Log4j
        mapJulToLog4j(java.util.logging.Level.ALL, Level.ALL);
        mapJulToLog4j(java.util.logging.Level.FINEST, LevelTranslator.FINEST);
        mapJulToLog4j(java.util.logging.Level.FINER, Level.TRACE);
        mapJulToLog4j(java.util.logging.Level.FINE, Level.DEBUG);
        mapJulToLog4j(java.util.logging.Level.CONFIG, LevelTranslator.CONFIG);
        mapJulToLog4j(java.util.logging.Level.INFO, Level.INFO);
        mapJulToLog4j(java.util.logging.Level.WARNING, Level.WARN);
        mapJulToLog4j(java.util.logging.Level.SEVERE, Level.ERROR);
        mapJulToLog4j(java.util.logging.Level.OFF, Level.OFF);
        // Map Log4j to JUL
        mapLog4jToJul(Level.ALL, java.util.logging.Level.ALL);
        mapLog4jToJul(LevelTranslator.FINEST, java.util.logging.Level.FINEST);
        mapLog4jToJul(Level.TRACE, java.util.logging.Level.FINER);
        mapLog4jToJul(Level.DEBUG, java.util.logging.Level.FINE);
        mapLog4jToJul(LevelTranslator.CONFIG, java.util.logging.Level.CONFIG);
        mapLog4jToJul(Level.INFO, java.util.logging.Level.INFO);
        mapLog4jToJul(Level.WARN, java.util.logging.Level.WARNING);
        mapLog4jToJul(Level.ERROR, java.util.logging.Level.SEVERE);
        mapLog4jToJul(Level.FATAL, java.util.logging.Level.SEVERE);
        mapLog4jToJul(Level.OFF, java.util.logging.Level.OFF);
        // Sorted Java levels
        sortedJulLevels.addAll(julToLog4j.keySet());
        Collections.sort(sortedJulLevels, new JulLevelComparator());

    }
    private Level mapCustomJulLevel(java.util.logging.Level customJavaLevel, java.util.logging.Level stdJavaLevel) {
        final Level level = julToLog4j.get(stdJavaLevel);
        julToLog4j.put(customJavaLevel, level);
        return level;
    }
    private long distance(java.util.logging.Level javaLevel, java.util.logging.Level customJavaLevel) {
        return Math.abs((long) customJavaLevel.intValue() - (long) javaLevel.intValue());
    }
        public int compare(java.util.logging.Level level1, java.util.logging.Level level2) {
            return Integer.compare(level1.intValue(), level2.intValue());
        }
    public Level toLevel(final java.util.logging.Level javaLevel) {
        final Level level = julToLog4j.get(javaLevel);
        return level != null ? level : addCustomJulLevel(javaLevel);
    }
