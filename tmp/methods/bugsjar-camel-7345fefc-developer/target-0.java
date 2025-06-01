    public void bindToExchange(Exchange exchange) {
        Map<String, Object> headers;

        exchange.setProperty(FileComponent.FILE_EXCHANGE_FILE, this);
        GenericFileMessage<T> msg = new GenericFileMessage<T>(this);
        if (exchange.hasOut()) {
            headers = exchange.getOut().hasHeaders() ? exchange.getOut().getHeaders() : null;
            exchange.setOut(msg);
        } else {
            headers = exchange.getIn().hasHeaders() ? exchange.getIn().getHeaders() : null;
            exchange.setIn(msg);
        }

        // preserve any existing (non file) headers, before we re-populate headers
        if (headers != null) {
            msg.setHeaders(headers);
            // remove any file related headers, as we will re-populate file headers
            msg.removeHeaders("CamelFile*");
        }
        populateHeaders(msg);
    }
