    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final Message msg = event.getMessage();
        if (msg != null) {
            String result;
            if (msg instanceof MultiformatMessage) {
                result = ((MultiformatMessage) msg).getFormattedMessage(formats);
            } else {
                result = msg.getFormattedMessage();
            }
            toAppendTo.append(config != null && result.contains("${") ?
                config.getSubst().replace(event, result) : result);
        }
    }
