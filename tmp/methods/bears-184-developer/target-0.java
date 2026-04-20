	public boolean isSingleIdAttribute(Class<?> entity, String name, Class<?> attributeType) {

		return metamodel.getEntities().stream() //
				.filter(it -> entity.equals(it.getJavaType())) //
				.findFirst() //
				.flatMap(it -> getSingularIdAttribute(it)) //
				.filter(it -> it.getJavaType().equals(attributeType)) //
				.map(it -> it.getName().equals(name)) //
				.orElse(false);
	}
