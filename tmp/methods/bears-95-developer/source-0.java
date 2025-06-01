	private static boolean isTypeInjectable(PersistentEntity<?, ?> entity) {
		return entity.getType().getClassLoader() != null && !entity.getType().getPackage().getName().startsWith("java");
	}
