    public void onCompletion(Exchange exchange) {
        if (exchange != null && isStoreAsBodyOnCompletion()) {
            List<V> list = (List<V>) exchange.removeProperty(Exchange.GROUPED_EXCHANGE);
            if (list != null) {
                exchange.getIn().setBody(list);
            }
        }
    }
