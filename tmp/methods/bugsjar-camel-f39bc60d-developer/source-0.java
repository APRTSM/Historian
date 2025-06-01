    public boolean process(Exchange exchange, AsyncCallback callback) {
        String msg = expression.evaluate(exchange, String.class);
        logger.log(msg);
        return true;
    }
