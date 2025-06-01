    public boolean process(Exchange exchange, AsyncCallback callback) {
        // use atomic integer to be able to pass reference and keep track on the values
        AtomicInteger index = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();

        // Intermediate conversion to String is needed when direct conversion to Integer is not available
        // but evaluation result is a textual representation of a numeric value.
        String text = expression.evaluate(exchange, String.class);
        try {
            int num = ExchangeHelper.convertToMandatoryType(exchange, Integer.class, text);
            count.set(num);
        } catch (NoTypeConversionAvailableException e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        }

        // we hold on to the original Exchange in case it's needed for copies
        final Exchange original = exchange;

        // per-iteration exchange
        Exchange target = exchange;

        // set the size before we start
        exchange.setProperty(Exchange.LOOP_SIZE, count);

        // loop synchronously
        while (index.get() < count.get()) {

            // and prepare for next iteration
            // if (!copy) target = exchange; else copy of original
            target = prepareExchange(exchange, index.get(), original);
            boolean sync = process(target, callback, index, count, original);

            if (!sync) {
                LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", target.getExchangeId());
                // the remainder of the routing slip will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            LOG.trace("Processing exchangeId: {} is continued being processed synchronously", target.getExchangeId());

            // increment counter before next loop
            index.getAndIncrement();
        }

        // we are done so prepare the result
        ExchangeHelper.copyResults(exchange, target);
        LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
        callback.done(true);
        return true;
    }
    protected Exchange prepareExchange(Exchange exchange, int index, Exchange original) {
        if (copy) {
            // use a copy but let it reuse the same exchange id so it appear as one exchange
            // use the original exchange rather than the looping exchange (esp. with the async routing engine)
            return ExchangeHelper.createCopy(original, true);
        } else {
            ExchangeHelper.prepareOutToIn(exchange);
            return exchange;
        }
    }
    protected boolean process(final Exchange exchange, final AsyncCallback callback,
                              final AtomicInteger index, final AtomicInteger count,
                              final Exchange original) {

        // set current index as property
        LOG.debug("LoopProcessor: iteration #{}", index.get());
        exchange.setProperty(Exchange.LOOP_INDEX, index.get());

        boolean sync = processor.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                // we only have to handle async completion of the routing slip
                if (doneSync) {
                    return;
                }

                Exchange target = exchange;

                // increment index as we have just processed once
                index.getAndIncrement();

                // continue looping asynchronously
                while (index.get() < count.get()) {

                    // and prepare for next iteration
                    target = prepareExchange(exchange, index.get(), original);

                    // process again
                    boolean sync = process(target, callback, index, count, original);
                    if (!sync) {
                        LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", target.getExchangeId());
                        // the remainder of the routing slip will be completed async
                        // so we break out now, then the callback will be invoked which then continue routing from where we left here
                        return;
                    }

                    // increment counter before next loop
                    index.getAndIncrement();
                }

                // we are done so prepare the result
                ExchangeHelper.copyResults(exchange, target);
                LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
                callback.done(false);
            }
        });

        return sync;
    }
