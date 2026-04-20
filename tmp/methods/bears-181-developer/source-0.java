	public long count(Class entityClass) {
		SpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		ResultSet resultSet = this.databaseClient.singleUse().executeQuery(Statement.of(
				String.format("select count(*) from %s", persistentEntity.tableName())));
		resultSet.next();
		return resultSet.getLong(0);
	}
