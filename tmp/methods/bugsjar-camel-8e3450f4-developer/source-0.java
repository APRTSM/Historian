    public void process(Exchange exchange) throws Exception {
        ObjectHelper.notNull(dataFormat, "dataFormat");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Message in = exchange.getIn();
        Object body = in.getBody();

        // lets setup the out message before we invoke the dataFormat
        // so that it can mutate it if necessary
        Message out = exchange.getOut();
        out.copyFrom(in);

        dataFormat.marshal(exchange, body, buffer);
        byte[] data = buffer.toByteArray();
        out.setBody(data);
    }
