    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        Info info = threadlocalInfo.get();
        if (info == null) {
            info = new Info(new RingBufferLogEventTranslator(), Thread.currentThread().getName(), false);
            threadlocalInfo.set(info);
        }

        // LOG4J2-471: prevent deadlock when RingBuffer is full and object
        // being logged calls Logger.log() from its toString() method
        if (info.isAppenderThread && disruptor.getRingBuffer().remainingCapacity() == 0) {
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

        disruptor.publishEvent(info.translator);
    }
    public boolean callAppendersFromAnotherThread(final LogEvent event) {

        // LOG4J2-471: prevent deadlock when RingBuffer is full and object
        // being logged calls Logger.log() from its toString() method
        if (isAppenderThread.get() == Boolean.TRUE //
                && disruptor.getRingBuffer().remainingCapacity() == 0) {

            // bypass RingBuffer and invoke Appender directly
            return false;
        }
        disruptor.getRingBuffer().publishEvent(translator, event, asyncLoggerConfig);
        return true;
    }
