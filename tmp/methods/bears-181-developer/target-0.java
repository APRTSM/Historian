	public long count(Class entityClass) {
		SpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		Statement statement = Statement.of(String.format(
				"select count(*) from %s", persistentEntity.tableName()));
		try (ResultSet resultSet = this.databaseClient.singleUse().executeQuery(statement)) {
			resultSet.next();
			return resultSet.getLong(0);
		}
	}
