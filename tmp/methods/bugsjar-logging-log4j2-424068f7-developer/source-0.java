    public void log(final LogRecord record) {
        if (isFiltered(record)) {
            return;
        }
        final org.apache.logging.log4j.Level level = LevelTranslator.toLevel(record.getLevel());
        final Message message = logger.getMessageFactory().newMessage(record.getMessage(), record.getParameters());
        final Throwable thrown = record.getThrown();
        logger.logIfEnabled(FQCN, level, null, message, thrown);
    }
