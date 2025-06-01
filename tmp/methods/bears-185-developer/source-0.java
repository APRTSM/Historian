	public NativeJpaQuery(JpaQueryMethod method, EntityManager em, String queryString,
			EvaluationContextProvider evaluationContextProvider, SpelExpressionParser parser) {

		super(method, em, queryString, evaluationContextProvider, parser);

		Parameters<?, ?> parameters = method.getParameters();
		boolean hasPagingOrSortingParameter = parameters.hasPageableParameter() || parameters.hasSortParameter();
		boolean containsPageableOrSortInQueryExpression = queryString.contains("#pageable")
				|| queryString.contains("#sort");

		if (hasPagingOrSortingParameter && !containsPageableOrSortInQueryExpression) {
			throw new InvalidJpaQueryMethodException(
					"Cannot use native queries with dynamic sorting and/or pagination in method " + method);
		}

		this.resultType = getTypeToQueryFor();
	}
