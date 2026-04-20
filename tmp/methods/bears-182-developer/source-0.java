	private List<String> getTags(String sql) {
		Pattern pattern = Pattern.compile("@\\S+");
		Matcher matcher = pattern.matcher(sql);
		List<String> tags = new ArrayList<>();
		while (matcher.find()) {
			// The initial '@' character must be excluded for Spanner
			tags.add(matcher.group().substring(1));
		}
		return tags;
	}
	private String resolveEntityClassNames(String sql) {
		StringJoiner joiner = new StringJoiner(" ");
		for (String part : sql.split("\\s+")) {
			if (part.length() > 2 && part.startsWith(ENTITY_CLASS_NAME_BOOKEND)
					&& part.endsWith(ENTITY_CLASS_NAME_BOOKEND)) {
				String className = part.substring(1, part.length() - 1);
				try {
					Class entityClass = Class.forName(className);
					SpannerPersistentEntity spannerPersistentEntity = this.spannerMappingContext
							.getPersistentEntity(entityClass);
					if (spannerPersistentEntity == null) {
						throw new SpannerDataException(
								"The class used in the SQL statement is not a Spanner persistent entity: "
										+ className);
					}
					joiner.add(spannerPersistentEntity.tableName());
				}
				catch (ClassNotFoundException e) {
					throw new SpannerDataException(
							"The class name does not refer to an available entity type: "
									+ className);
				}
			}
			else {
				joiner.add(part);
			}
		}
		return joiner.toString();
	}
