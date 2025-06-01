    public Result executeQuery(String statement, String language, long limit,
            long offset, Map<String, ? extends PropertyValue> bindings,
            NamePathMapper namePathMapper) throws ParseException {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit may not be negative, is: " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset may not be negative, is: " + offset);
        }
        Query q = parseQuery(statement, language);
        q.setRootTree(getRootTree());
        q.setRootState(getRootState());
        q.setNamePathMapper(namePathMapper);
        q.setLimit(limit);
        q.setOffset(offset);
        if (bindings != null) {
            for (Entry<String, ? extends PropertyValue> e : bindings.entrySet()) {
                q.bindValue(e.getKey(), e.getValue());
            }
        }
        q.setQueryEngine(this);
        q.prepare();
        return q.executeQuery();
    }
