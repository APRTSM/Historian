    public void close() throws IOException
    {
        if (!isClosed()) {
            _emitter.emit(new DocumentEndEvent(null, null, false));
            _emitter.emit(new StreamEndEvent(null, null));
            super.close();

            /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
             *   on the underlying Reader, unless we "own" it, or auto-closing
             *   feature is enabled.
             *   One downside: when using UTF8Writer, underlying buffer(s)
             *   may not be properly recycled if we don't close the writer.
             */
            if (_writer != null) {
                if (_ioContext.isResourceManaged() || isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET)) {
                    _writer.close();
                } else  if (isEnabled(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)) {
                    // If we can't close it, we should at least flush
                    _writer.flush();
                }
            }
        }
    }
