    public void sendEvents(final Layout<?> layout, final LogEvent appendEvent) {
        if (message == null) {
            connect();
        }
        try {
            final LogEvent[] priorEvents = buffer.removeAll();
            if (priorEvents == null || priorEvents.length == 0) {
                return; // nothing to do, another thread already took all events
            }
            final byte[] rawBytes = formatContentToBytes(priorEvents, appendEvent, layout);

            final String contentType = layout.getContentType();
            final String encoding = getEncoding(rawBytes, contentType);
            final byte[] encodedBytes = encodeContentToBytes(rawBytes, encoding);

            final InternetHeaders headers = getHeaders(contentType, encoding);
            final MimeMultipart mp = getMimeMultipart(encodedBytes, headers);

            sendMultipartMessage(message, mp);
        } catch (final MessagingException e) {
            LOGGER.error("Error occurred while sending e-mail notification.", e);
            throw new LoggingException("Error occurred while sending email", e);
        } catch (final IOException e) {
            LOGGER.error("Error occurred while sending e-mail notification.", e);
            throw new LoggingException("Error occurred while sending email", e);
        } catch (final RuntimeException e) {
            LOGGER.error("Error occurred while sending e-mail notification.", e);
            throw new LoggingException("Error occurred while sending email", e);
        }
    }
