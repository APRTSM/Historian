    public static String extractValueForLogging(Object obj, Message message, String prepend, boolean allowStreams, boolean allowFiles, int maxChars) {
        if (maxChars < 0) {
            return prepend + "[Body is not logged]";
        }

        if (obj == null) {
            return prepend + "[Body is null]";
        }

        if (!allowStreams) {
            if (obj instanceof Source && !(obj instanceof StringSource || obj instanceof BytesSource)) {
                // for Source its only StringSource or BytesSource that is okay as they are memory based
                // all other kinds we should not touch the body
                return prepend + "[Body is instance of java.xml.transform.Source]";
            } else if (obj instanceof StreamCache) {
                return prepend + "[Body is instance of org.apache.camel.StreamCache]";
            } else if (obj instanceof InputStream) {
                return prepend + "[Body is instance of java.io.InputStream]";
            } else if (obj instanceof OutputStream) {
                return prepend + "[Body is instance of java.io.OutputStream]";
            } else if (obj instanceof Reader) {
                return prepend + "[Body is instance of java.io.Reader]";
            } else if (obj instanceof Writer) {
                return prepend + "[Body is instance of java.io.Writer]";
            } else if (obj instanceof WrappedFile || obj instanceof File) {
                if (!allowFiles) {
                    return prepend + "[Body is file based: " + obj + "]";
                }
            }
        }

        if (!allowFiles) {
            if (obj instanceof WrappedFile || obj instanceof File) {
                return prepend + "[Body is file based: " + obj + "]";
            }
        }

        // is the body a stream cache
        StreamCache cache;
        if (obj instanceof StreamCache) {
            cache = (StreamCache)obj;
        } else {
            cache = null;
        }

        // grab the message body as a string
        String body = null;
        if (message.getExchange() != null) {
            try {
                body = message.getExchange().getContext().getTypeConverter().convertTo(String.class, message.getExchange(), obj);
            } catch (Exception e) {
                // ignore as the body is for logging purpose
            }
        }
        if (body == null) {
            body = obj.toString();
        }

        // reset stream cache after use
        if (cache != null) {
            cache.reset();
        }

        if (body == null) {
            return prepend + "[Body is null]";
        }

        // clip body if length enabled and the body is too big
        if (maxChars > 0 && body.length() > maxChars) {
            body = body.substring(0, maxChars) + "... [Body clipped after " + maxChars + " chars, total length is " + body.length() + "]";
        }

        return prepend + body;
    }
