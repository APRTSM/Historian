    protected long calculateDelay(Exchange exchange) {
        // evaluate as Object first to see if we get any result at all
        Object result = maxRequestsPerPeriodExpression.evaluate(exchange, Object.class);
        if (result == null) {
            throw new RuntimeExchangeException("The max requests per period expression was evaluated as null: " + maxRequestsPerPeriodExpression, exchange);
        }

        // then must convert value to long
        Long longValue = exchange.getContext().getTypeConverter().convertTo(Long.class, result);
        if (longValue != null) {
            // log if we changed max period after initial setting
            if (maximumRequestsPerPeriod > 0 && longValue.longValue() != maximumRequestsPerPeriod) {
                log.debug("Throttler changed maximum requests per period from {} to {}", maximumRequestsPerPeriod, longValue);
            }
            if (maximumRequestsPerPeriod > longValue) {
                slot.capacity = 0;
            }
            maximumRequestsPerPeriod = longValue;
        }

        if (maximumRequestsPerPeriod <= 0) {
            throw new IllegalStateException("The maximumRequestsPerPeriod must be a positive number, was: " + maximumRequestsPerPeriod);
        }

        TimeSlot slot = nextSlot();
        if (!slot.isActive()) {
            long delay = slot.startTime - currentSystemTime();
            return delay;
        } else {
            return 0;
        }
    }
        protected boolean isPast() {
            long current = System.currentTimeMillis();
            return current < (startTime + duration);
        }
    protected synchronized TimeSlot nextSlot() {
        if (slot == null) {
            slot = new TimeSlot();
        }
        if (slot.isFull() || !slot.isPast()) {
            slot = slot.next();
        }
        slot.assign();
        return slot;
    }
        protected boolean isFull() {
            return capacity <= 0;
        }
