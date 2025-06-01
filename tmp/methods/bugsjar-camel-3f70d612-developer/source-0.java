    public NotifyBuilder whenDoneByIndex(final int index) {
        stack.add(new EventPredicateSupport() {
            private AtomicInteger current = new AtomicInteger();
            private String id;
            private AtomicBoolean done = new AtomicBoolean();

            @Override
            public boolean onExchangeCreated(Exchange exchange) {
                if (current.get() == index) {
                    id = exchange.getExchangeId();
                }
                current.incrementAndGet();
                return true;
            }

            @Override
            public boolean onExchangeCompleted(Exchange exchange) {
                if (exchange.getExchangeId().equals(id)) {
                    done.set(false);
                }
                return true;
            }

            @Override
            public boolean onExchangeFailed(Exchange exchange) {
                if (exchange.getExchangeId().equals(id)) {
                    done.set(true);
                }
                return true;
            }

            public boolean matches() {
                return done.get();
            }

            @Override
            public void reset() {
                current.set(0);
                id = null;
                done.set(false);
            }

            @Override
            public String toString() {
                return "whenDoneByIndex(" + index + ")";
            }
        });
        return this;
    }
