    public synchronized void failedExchange(Exchange exchange) {
        InFlightKey key = exchangesInFlightKeys.remove(exchange.getExchangeId());
        if (key != null) {
            exchangesInFlightStartTimestamps.remove(key);
        }
        super.failedExchange(exchange);
    }
        InFlightKey(Long timeStamp, String exchangeId) {
            this.timeStamp = timeStamp;
            this.exchangeId = exchangeId;
        }
    public void init(ManagementStrategy strategy) {
        exchangesInFlightStartTimestamps.clear();
        super.init(strategy);
    }
