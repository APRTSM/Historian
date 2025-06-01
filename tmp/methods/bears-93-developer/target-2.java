	static Object[] allocateArguments(int argumentCount) {
		return argumentCount == 0 ? EMPTY_ARGS : new Object[argumentCount];
	}
		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> T createInstance(E entity,
				ParameterValueProvider<P> provider) {

			Object[] params = extractInvocationArguments(entity.getPersistenceConstructor(), provider);

			try {
				return (T) instantiator.newInstance(params);
			} catch (Exception e) {
				throw new MappingInstantiationException(entity, Arrays.asList(params), e);
			}
		}
		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> T createInstance(E entity,
				ParameterValueProvider<P> provider) {

			PreferredConstructor<? extends T, P> preferredConstructor = entity.getPersistenceConstructor();

			if (preferredConstructor == null) {
				throw new IllegalArgumentException("PreferredConstructor must not be null!");
			}

			int[] defaulting = new int[(synthetic.getParameterCount() / 32) + 1];

			Object[] params = allocateArguments(
					synthetic.getParameterCount() + defaulting.length + /* DefaultConstructorMarker */1);
			int userParameterCount = kParameters.size();

			List<Parameter<Object, P>> parameters = preferredConstructor.getParameters();

			// Prepare user-space arguments
			for (int i = 0; i < userParameterCount; i++) {

				int slot = i / 32;
				int offset = slot * 32;

				Object param = provider.getParameterValue(parameters.get(i));

				KParameter kParameter = kParameters.get(i);

				// what about null and parameter is mandatory? What if parameter is non-null?
				if (kParameter.isOptional()) {

					if (param == null) {
						defaulting[slot] = defaulting[slot] | (1 << (i - offset));
					}
				}

				params[i] = param;
			}

			// append nullability masks to creation arguments
			for (int i = 0; i < defaulting.length; i++) {
				params[userParameterCount + i] = defaulting[i];
			}

			return (T) instantiator.newInstance(params);
		}
	public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> T createInstance(E entity,
			ParameterValueProvider<P> provider) {

		PreferredConstructor<? extends T, P> constructor = entity.getPersistenceConstructor();

		if (constructor == null) {

			try {
				Class<?> clazz = entity.getType();
				if (clazz.isArray()) {
					Class<?> ctype = clazz;
					int dims = 0;
					while (ctype.isArray()) {
						ctype = ctype.getComponentType();
						dims++;
					}
					return (T) Array.newInstance(clazz, dims);
				} else {
					return BeanUtils.instantiateClass(entity.getType());
				}
			} catch (BeanInstantiationException e) {
				throw new MappingInstantiationException(entity, Collections.emptyList(), e);
			}
		}
		int parameterCount = constructor.getConstructor().getParameterCount();

		Object[] params = parameterCount == 0 ? EMPTY_ARGS : new Object[parameterCount];
		int i = 0;
		for (Parameter<?, P> parameter : constructor.getParameters()) {
			params[i++] = provider.getParameterValue(parameter);
		}

		try {
			return BeanUtils.instantiateClass(constructor.getConstructor(), params);
		} catch (BeanInstantiationException e) {
			throw new MappingInstantiationException(entity, new ArrayList<>(Arrays.asList(params)), e);
		}
	}
