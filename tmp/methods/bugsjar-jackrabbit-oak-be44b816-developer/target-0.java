    public QueryResult execute() throws RepositoryException {
        return sessionContext.getSessionDelegate().perform(
                new SessionOperation<QueryResult>() {
                    @Override
                    public QueryResult perform() throws RepositoryException {
                        return manager.executeQuery(statement, language, limit,
                                offset, bindVariableMap);
                    }
                });
    }
