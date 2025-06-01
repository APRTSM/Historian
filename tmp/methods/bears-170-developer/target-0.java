	public Iterable<AlgorithmEvaluation> applyAlgorithm(Attribute targetAttribute, String algorithm,
			Iterable<Entity> sourceEntities)
	{
		return stream(sourceEntities.spliterator(), false).map(entity -> {
			AlgorithmEvaluation algorithmResult = new AlgorithmEvaluation(entity);
			Object derivedValue;

			try
			{
				Object result = jsMagmaScriptEvaluator.eval(algorithm, entity);

				// jsMagmaScriptEvaluator.eval() catches and returns the error instead of throwing it
				// so check instance of result object here
				if (result instanceof ScriptException)
				{
					return algorithmResult.errorMessage(((ScriptException) result).getMessage());
				}

				derivedValue = convert(result, targetAttribute);
			}
			catch (RuntimeException e)
			{
				if (e.getMessage() == null)
				{
					return algorithmResult.errorMessage(
							"Applying an algorithm on a null source value caused an exception. Is the target attribute required?");
				}
				return algorithmResult.errorMessage(e.getMessage());
			}
			return algorithmResult.value(derivedValue);
		}).collect(toList());
	}
	public Object apply(AttributeMapping attributeMapping, Entity sourceEntity, EntityType sourceEntityType)
	{
		String algorithm = attributeMapping.getAlgorithm();
		if (isEmpty(algorithm))
		{
			return null;
		}
		Object result = jsMagmaScriptEvaluator.eval(algorithm, sourceEntity);

		// jsMagmaScriptEvaluator.eval() catches and returns the error instead of throwing it
		// so check instance of result object here
		if (result instanceof ScriptException)
		{
			throw new ScriptException(((ScriptException) result).getMessage(), ((ScriptException) result).getCause());
		}

		return convert(result, attributeMapping.getTargetAttribute());
	}
