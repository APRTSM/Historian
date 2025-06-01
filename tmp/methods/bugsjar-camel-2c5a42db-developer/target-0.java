    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<V> list;

        if (oldExchange == null) {
            list = getList(newExchange);
        } else {
            list = getList(oldExchange);
        }

        if (newExchange != null) {
            V value = getValue(newExchange);
            if (value != null) {
                list.add(value);
            }
        }

        return oldExchange != null ? oldExchange : newExchange;
    }
