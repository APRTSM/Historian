	public MatchResult match(Class<?> sourceType, Class<?> destinationType) {
		if (Value.class.isAssignableFrom(sourceType) &&
				Value.class.isAssignableFrom(destinationType)) {
			return MatchResult.FULL;
		} else {
			return MatchResult.NONE;
		}
	}
	public Value convert(MappingContext<Value, Value> context) {
		if (context == null || context.getSource() == null) {
			return null;
		}
		final Value<?> source = (Value<?>) context.getSource();
		final PropertyInfo destInfo = context.getMapping().getLastDestinationProperty();
		final Class<?> destinationType = TypeResolver
				.resolveRawArgument(destInfo.getGenericType(), destInfo.getInitialType());
		return source
				.map(src -> context.create(src, destinationType))
				.map(ctx -> context.getMappingEngine().map(ctx));
	}
