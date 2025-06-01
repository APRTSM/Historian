	static Object[] allocateArguments(int argumentCount) {
		return argumentCount < ARG_CACHE_SIZE ? OBJECT_POOL.get()[argumentCount] : new Object[argumentCount];
	}
		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> T createInstance(E entity,
				ParameterValueProvider<P> provider) {

			Object[] params = extractInvocationArguments(entity.getPersistenceConstructor(), provider);

			try {
				return (T) instantiator.newInstance(params);
			} catch (Exception e) {
				throw new MappingInstantiationException(entity, Arrays.asList(params), e);
			} finally {
				deallocateArguments(params);
			}
		}
	static void deallocateArguments(Object[] params) {

		if (params.length != 0 && params.length < ARG_CACHE_SIZE) {
			Arrays.fill(params, null);
		}
	}
