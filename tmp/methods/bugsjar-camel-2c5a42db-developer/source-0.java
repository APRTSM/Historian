    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<V> list;
        Exchange answer = oldExchange;

        if (oldExchange == null) {
            answer = new DefaultExchange(newExchange);
            list = getList(answer);
        } else {
            list = getList(oldExchange);
        }

        if (newExchange != null) {
            V value = getValue(newExchange);
            if (value != null) {
                list.add(value);
            }
        }

        return answer;
    }
