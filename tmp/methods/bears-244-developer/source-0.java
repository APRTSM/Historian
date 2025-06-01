	boolean shouldUseReflectionEntityInstantiator(PersistentEntity<?, ?> entity) {

		Class<?> type = entity.getType();

		if (type.isInterface() //
				|| type.isArray() //
				|| Modifier.isPrivate(type.getModifiers()) //
				|| (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) //
				|| ClassUtils.isCglibProxyClass(type)) { //
			return true;
		}

		PreferredConstructor<?, ?> persistenceConstructor = entity.getPersistenceConstructor();
		if (persistenceConstructor == null || Modifier.isPrivate(persistenceConstructor.getConstructor().getModifiers())) {
			return true;
		}

		return false;
	}
