    public void append(final LogEvent logEvent) {
        if (!isStarted()) {
            throw new IllegalStateException("AsyncAppender " + getName() + " is not active");
        }
        if (!(logEvent instanceof Log4jLogEvent)) {
            return; // only know how to Serialize Log4jLogEvents
        }
        Log4jLogEvent coreEvent = (Log4jLogEvent) logEvent;
        boolean appendSuccessful = false;
        if (blocking) {
            if (isAppenderThread.get() == Boolean.TRUE && queue.remainingCapacity() == 0) {
                // LOG4J2-485: avoid deadlock that would result from trying
                // to add to a full queue from appender thread
                coreEvent.setEndOfBatch(false); // queue is definitely not empty!
                appendSuccessful = thread.callAppenders(coreEvent);
            } else {
                try {
                    // wait for free slots in the queue
                    queue.put(Log4jLogEvent.serialize(coreEvent, includeLocation));
                    appendSuccessful = true;
                } catch (final InterruptedException e) {
                    LOGGER.warn("Interrupted while waiting for a free slot in the AsyncAppender LogEvent-queue {}",
                            getName());
                }
            }
        } else {
            appendSuccessful = queue.offer(Log4jLogEvent.serialize(coreEvent, includeLocation));
            if (!appendSuccessful) {
                error("Appender " + getName() + " is unable to write primary appenders. queue is full");
            }
        }
        if (!appendSuccessful && errorAppender != null) {
            errorAppender.callAppender(coreEvent);
        }
    }
