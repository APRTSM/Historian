    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            // for the first time we must create a new empty exchange as the holder, as the outgoing exchange
            // must not be one of the grouped exchanges, as that causes a endless circular reference
            oldExchange = new DefaultExchange(newExchange);
        }
        return super.aggregate(oldExchange, newExchange);
    }
