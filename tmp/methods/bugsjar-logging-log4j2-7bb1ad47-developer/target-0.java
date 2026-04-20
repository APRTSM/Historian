    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message msg, 
            final Throwable throwable) {
        final StringBuilder sb = new StringBuilder();
        // Append date-time if so configured
        if (showDateTime) {
            final Date now = new Date();
            String dateText;
            synchronized (dateFormatter) {
                dateText = dateFormatter.format(now);
            }
            sb.append(dateText);
            sb.append(SPACE);
        }

        sb.append(level.toString());
        sb.append(SPACE);
        if (logName != null && logName.length() > 0) {
            sb.append(logName);
            sb.append(SPACE);
        }
        sb.append(msg.getFormattedMessage());
        if (showContextMap) {
            final Map<String, String> mdc = ThreadContext.getContext();
            if (mdc.size() > 0) {
                sb.append(SPACE);
                sb.append(mdc.toString());
                sb.append(SPACE);
            }
        }
        final Object[] params = msg.getParameters();
        Throwable t;
        if (throwable == null && params != null && params.length > 0 && params[params.length - 1] instanceof Throwable) {
            t = (Throwable) params[params.length - 1];
        } else {
            t = throwable;
        }
        if (t != null) {
            sb.append(SPACE);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(baos));
            sb.append(baos.toString());
        }
        stream.println(sb.toString());
    }
