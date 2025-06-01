    public boolean process(Exchange exchange, AsyncCallback callback) {
        final String messageId = messageIdExpression.evaluate(exchange, String.class);
        if (messageId == null) {
            throw new NoMessageIdException(exchange, messageIdExpression);
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

        // register our on completion callback
        exchange.addOnCompletion(new IdempotentOnCompletion(idempotentRepository, messageId, eager, removeOnFailure));

        // process the exchange
        return processor.process(exchange, callback);
    }
