    public void bindToExchange(Exchange exchange) {
        exchange.setProperty(FileComponent.FILE_EXCHANGE_FILE, this);
        GenericFileMessage<T> in = new GenericFileMessage<T>(this);
        exchange.setIn(in);
        populateHeaders(in);
    }
