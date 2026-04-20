    public void close() throws IOException
    {
        if (!isClosed()) {
            _emitter.emit(new DocumentEndEvent(null, null, false));
            _emitter.emit(new StreamEndEvent(null, null));
            super.close();
            _writer.close();
        }
    }
    protected void _closeInput() throws IOException {
        _reader.close();
    }
