    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        if (msg == null) {
            return onMismatch;
        }
        return filter(msg.toString());
    }
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        if (msg == null) {
            return onMismatch;
        }
        final String text = useRawMessage ? msg.getFormat() : msg.getFormattedMessage();
        return filter(text);
    }
