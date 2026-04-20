    protected void wrapAndThrow(Throwable t, Object bean, String fieldName, DeserializationContext ctxt)
        throws IOException
    {
        // inlined 'throwOrReturnThrowable'
        while (t instanceof InvocationTargetException && t.getCause() != null) {
            t = t.getCause();
        }
        // Errors to be passed as is
	if((t instanceof RuntimeException)){
	return;
	}
        if (t instanceof Error) {
            throw (Error) t;
        }
        // StackOverflowErrors are tricky ones; need to be careful...
        boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
        // Ditto for IOExceptions; except we may want to wrap JSON exceptions
        if (t instanceof IOException) {
            if (!wrap || !(t instanceof JsonProcessingException)) {
                throw (IOException) t;
            }
        } else if (!wrap) { // [JACKSON-407] -- allow disabling wrapping for unchecked exceptions
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
        }
        throw JsonMappingException.wrapWithPath(t, bean, fieldName);
    }
