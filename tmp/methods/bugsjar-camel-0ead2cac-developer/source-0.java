    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        final String messageId = messageIdExpression.evaluate(exchange, String.class);
        if (messageId == null) {
            exchange.setException(new NoMessageIdException(exchange, messageIdExpression));
            callback.done(true);
            return true;
        }

        boolean newKey;
        if (eager) {
            // add the key to the repository
            if (idempotentRepository instanceof ExchangeIdempotentRepository) {
                newKey = ((ExchangeIdempotentRepository<String>) idempotentRepository).add(exchange, messageId);
            } else {
                newKey = idempotentRepository.add(messageId);
            }
        } else {
            // check if we already have the key
            if (idempotentRepository instanceof ExchangeIdempotentRepository) {
                newKey = ((ExchangeIdempotentRepository<String>) idempotentRepository).contains(exchange, messageId);
            } else {
                newKey = !idempotentRepository.contains(messageId);
            }
        }


        if (!newKey) {
            // mark the exchange as duplicate
            exchange.setProperty(Exchange.DUPLICATE_MESSAGE, Boolean.TRUE);

            // we already have this key so its a duplicate message
            onDuplicate(exchange, messageId);

            if (skipDuplicate) {
                // if we should skip duplicate then we are done
                LOG.debug("Ignoring duplicate message with id: {} for exchange: {}", messageId, exchange);
                callback.done(true);
                return true;
            }
        }

        final Synchronization onCompletion = new IdempotentOnCompletion(idempotentRepository, messageId, eager, removeOnFailure);
        final AsyncCallback target = new IdempotentConsumerCallback(exchange, onCompletion, callback, completionEager);
        if (!completionEager) {
            // the scope is to do the idempotent completion work as an unit of work on the exchange when its done being routed
            exchange.addOnCompletion(onCompletion);
        }

        // process the exchange
        return processor.process(exchange, target);
    }
