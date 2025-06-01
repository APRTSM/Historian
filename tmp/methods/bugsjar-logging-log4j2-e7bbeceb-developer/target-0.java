    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        ThrowableProxy proxy = null;
        if (event instanceof Log4jLogEvent) {
            proxy = ((Log4jLogEvent) event).getThrownProxy();
        }
        final Throwable throwable = event.getThrown();
        if ((throwable != null || proxy != null) && options.anyLines()) {
            if (proxy == null) {
                super.format(event, toAppendTo);
                return;
            }
            final String extStackTrace = proxy.getExtendedStackTraceAsString(options.getPackages());
            final int len = toAppendTo.length();
            if (len > 0 && !Character.isWhitespace(toAppendTo.charAt(len - 1))) {
                toAppendTo.append(' ');
            }
            if (!options.allLines() || !Constants.LINE_SEPARATOR.equals(options.getSeparator())) {
                final StringBuilder sb = new StringBuilder();
                final String[] array = extStackTrace.split(Constants.LINE_SEPARATOR);
                final int limit = options.minLines(array.length) - 1;
                for (int i = 0; i <= limit; ++i) {
                    sb.append(array[i]);
                    if (i < limit) {
                        sb.append(options.getSeparator());
                    }
                }
                toAppendTo.append(sb.toString());

            } else {
                toAppendTo.append(extStackTrace);
            }
        }
    }
