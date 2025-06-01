    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        Info info = threadlocalInfo.get();
        if (info == null) {
            info = new Info(new RingBufferLogEventTranslator(), Thread.currentThread().getName(), false);
            threadlocalInfo.set(info);
        }

        Disruptor<RingBufferLogEvent> temp = disruptor;
        if (temp == null) { // LOG4J2-639
            LOGGER.fatal("Ignoring log event after log4j was shut down");
            return;
        }

        // LOG4J2-471: prevent deadlock when RingBuffer is full and object
        // being logged calls Logger.log() from its toString() method
        if (info.isAppenderThread && temp.getRingBuffer().remainingCapacity() == 0) {
            // bypass RingBuffer and invoke Appender directly
            config.loggerConfig.log(getName(), fqcn, marker, level, message, thrown);
            return;
        }
        final boolean includeLocation = config.loggerConfig.isIncludeLocation();
        info.translator.setValues(this, getName(), marker, fqcn, level, message, //
                // don't construct ThrowableProxy until required
                thrown, //

                // config properties are taken care of in the EventHandler
                // thread in the #actualAsyncLog method

                // needs shallow copy to be fast (LOG4J2-154)
                ThreadContext.getImmutableContext(), //

                // needs shallow copy to be fast (LOG4J2-154)
                ThreadContext.getImmutableStack(), //

                // Thread.currentThread().getName(), //
                // info.cachedThreadName, //
                THREAD_NAME_STRATEGY.getThreadName(info), // LOG4J2-467

                // location: very expensive operation. LOG4J2-153:
                // Only include if "includeLocation=true" is specified,
                // exclude if not specified or if "false" was specified.
                includeLocation ? location(fqcn) : null,

                // System.currentTimeMillis());
                // CoarseCachedClock: 20% faster than system clock, 16ms gaps
                // CachedClock: 10% faster than system clock, smaller gaps
                clock.currentTimeMillis());

        // LOG4J2-639: catch NPE if disruptor field was set to null after our check above
        try {
            // Note: do NOT use the temp variable above!
            // That could result in adding a log event to the disruptor after it was shut down,
            // which could cause the publishEvent method to hang and never return.
            disruptor.publishEvent(info.translator);
        } catch (NullPointerException npe) {
            LOGGER.fatal("Ignoring log event after log4j was shut down.");
        }
    }
