    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Exchange answer = super.aggregate(oldExchange, newExchange);
        if (oldExchange == null) {
            // for the first time we must do a copy as the answer, so the outgoing
            // exchange is not one of the grouped exchanges, as that causes a endless circular reference
            answer = answer.copy();
        }
        return answer;
    }
