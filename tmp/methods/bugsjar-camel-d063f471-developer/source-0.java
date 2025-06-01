        InFlightKey(Long timeStamp, String exchangeId) {
            this.exchangeId = exchangeId;
            this.timeStamp = timeStamp;
        }
    public void init(ManagementStrategy strategy) {
        super.init(strategy);
        exchangesInFlightStartTimestamps.clear();
    }
