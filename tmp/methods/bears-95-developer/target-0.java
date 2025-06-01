	private static boolean isTypeInjectable(PersistentEntity<?, ?> entity) {

		Class<?> type = entity.getType();
		return type.getClassLoader() != null
				&& (type.getPackage() == null || !type.getPackage().getName().startsWith("java"));
	}
