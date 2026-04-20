    public QueryResult execute() throws RepositoryException {
        return manager.executeQuery(statement, language, limit, offset, bindVariableMap);
    }
