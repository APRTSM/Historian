	public NativeJpaQuery(JpaQueryMethod method, EntityManager em, String queryString,
			EvaluationContextProvider evaluationContextProvider, SpelExpressionParser parser) {

		super(method, em, queryString, evaluationContextProvider, parser);

		Parameters<?, ?> parameters = method.getParameters();

		if (parameters.hasSortParameter() && !queryString.contains("#sort")) {
			throw new InvalidJpaQueryMethodException(
					"Cannot use native queries with dynamic sorting in method " + method);
		}

		this.resultType = getTypeToQueryFor();
	}
