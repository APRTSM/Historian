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
