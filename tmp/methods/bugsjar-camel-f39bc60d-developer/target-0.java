    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            String msg = expression.evaluate(exchange, String.class);
            logger.log(msg);
        } catch (Exception e) {
            exchange.setException(e);
        } finally {
            // callback must be invoked
            callback.done(true);
        }
        return true;
    }
