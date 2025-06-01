    private void appendMessage(final StringBuilder buffer, final LogEvent event) {
        final Message message = event.getMessage();
        final String text = message.getFormat();

        if (text != null && text.length() > 0) {
            buffer.append(" ").append(escapeNewlines(text, escapeNewLine));
        }

        if (exceptionFormatters != null && event.getThrown() != null) {
            final StringBuilder exception = new StringBuilder(LF);
            for (final PatternFormatter formatter : exceptionFormatters) {
                formatter.format(event, exception);
            }
            buffer.append(escapeNewlines(exception.toString(), escapeNewLine));
        }
        if (includeNewLine) {
            buffer.append(LF);
        }
    }
