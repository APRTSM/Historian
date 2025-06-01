	private String resolveEntityClassNames(String sql) {
		Pattern pattern = Pattern.compile("\\:\\S+\\:");
		Matcher matcher = pattern.matcher(sql);
		String result = sql;
		while (matcher.find()) {
			String matched = matcher.group();
			String className = matched.substring(1, matched.length() - 1);
			try {
				Class entityClass = Class.forName(className);
				SpannerPersistentEntity spannerPersistentEntity = this.spannerMappingContext
						.getPersistentEntity(entityClass);
				if (spannerPersistentEntity == null) {
					throw new SpannerDataException(
							"The class used in the SQL statement is not a Spanner persistent entity: "
									+ className);
				}
				result = result.replace(matched, spannerPersistentEntity.tableName());
			}
			catch (ClassNotFoundException e) {
				throw new SpannerDataException(
						"The class name does not refer to an available entity type: "
								+ className);
			}
		}
		return result;
	}
	private List<String> getTags(String sql) {
		Pattern pattern = Pattern.compile("@[^\\{^\\s][\\S]+[^\\}^\\s]");
		Matcher matcher = pattern.matcher(sql);
		List<String> tags = new ArrayList<>();
		while (matcher.find()) {
			// The initial '@' character must be excluded for Spanner
			tags.add(matcher.group().substring(1));
		}
		return tags;
	}
