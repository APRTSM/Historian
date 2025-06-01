    public void process(Exchange exchange) throws Exception {
        ObjectHelper.notNull(dataFormat, "dataFormat");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Message in = exchange.getIn();
        Object body = in.getBody();

        // lets setup the out message before we invoke the dataFormat
        // so that it can mutate it if necessary
        Message out = exchange.getOut();
        out.copyFrom(in);

        try {
            dataFormat.marshal(exchange, body, buffer);
            byte[] data = buffer.toByteArray();
            out.setBody(data);
        } catch (Exception e) {
            // remove OUT message, as an exception occurred
            exchange.setOut(null);
            throw e;
        }
    }
    public void process(Exchange exchange) throws Exception {
        ObjectHelper.notNull(dataFormat, "dataFormat");

        InputStream stream = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
        try {
            // lets setup the out message before we invoke the dataFormat
            // so that it can mutate it if necessary
            Message out = exchange.getOut();
            out.copyFrom(exchange.getIn());

            Object result = dataFormat.unmarshal(exchange, stream);
            out.setBody(result);
        } catch (Exception e) {
            // remove OUT message, as an exception occurred
            exchange.setOut(null);
            throw e;
        } finally {
            IOHelper.close(stream, "input stream");
        }
    }
