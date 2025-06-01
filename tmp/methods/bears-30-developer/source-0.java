	<T> CtVariableAccess<T> createVariableAccessNoClasspath(SingleNameReference singleNameReference) {
		final CoreFactory coreFactory = jdtTreeBuilder.getFactory().Core();
		final ExecutableFactory executableFactory = jdtTreeBuilder.getFactory().Executable();
		final ContextBuilder contextBuilder = jdtTreeBuilder.getContextBuilder();
		final ReferenceBuilder referenceBuilder = jdtTreeBuilder.getReferencesBuilder();
		final PositionBuilder positionBuilder = jdtTreeBuilder.getPositionBuilder();

		final String name = CharOperation.charToString(singleNameReference.token);
		final CtVariable<T> variable = contextBuilder.getVariableDeclaration(name);
		if (variable == null) {
			return null;
		}

		final CtVariableReference<T> variableReference;
		final CtVariableAccess<T> variableAccess;
		if (variable instanceof CtParameter) {
			// create variable of concrete type to avoid type casting while calling methods
			final CtParameterReference<T> parameterReference = coreFactory.createParameterReference();
			if (variable.getParent() instanceof CtLambda) {
				parameterReference.setDeclaringExecutable(
						referenceBuilder.getLambdaExecutableReference(singleNameReference));
			} else {
				// Unfortunately, we can not use `variable.getReference()` here as some parent
				// references (in terms of Java objects) have not been set up yet. Thus, we need to
				// create the required parameter reference by our own.

				// since the given parameter has not been declared in a lambda expression it must
				// have been declared by a method!
				final CtMethod method = (CtMethod) variable.getParent();

				// create list of method's parameter types
				final List<CtTypeReference<?>> parameterTypesOfMethod = new ArrayList<>();
				final List<CtParameter<?>> parametersOfMethod = method.getParameters();
				for (CtParameter<?> parameter : parametersOfMethod) {
					if (parameter.getType() != null) {
						parameterTypesOfMethod.add(parameter.getType().clone());
					}
				}

				// find method's corresponding jdt element
				MethodDeclaration methodJDT = null;
				for (final ASTPair astPair : contextBuilder.stack) {
					if (astPair.element == method) {
						methodJDT = (MethodDeclaration) astPair.node;
						break;
					}
				}
				assert methodJDT != null;

				// create a reference to method's declaring class
				final CtTypeReference declaringReferenceOfMethod =
						// `binding` may be null for anonymous classes which means we have to
						// create an 'empty' type reference since we have no further information
						methodJDT.binding == null ? coreFactory.createTypeReference()
								: referenceBuilder.getTypeReference(methodJDT.binding.declaringClass);

				// create a reference to the method of the currently processed parameter reference
				final CtExecutableReference methodReference =
						executableFactory.createReference(declaringReferenceOfMethod,
								// we need to clone method's return type (rt) before passing to
								// `createReference` since this method (indirectly) sets the parent
								// of the rt and, therefore, may break the AST
								method.getType().clone(),
								// no need to clone/copy as Strings are immutable
								method.getSimpleName(),
								// no need to clone/copy as we just created this object
								parameterTypesOfMethod);

				// finally, we can set the method reference...
				parameterReference.setDeclaringExecutable(methodReference);
			}
			variableReference = parameterReference;
			variableAccess = isLhsAssignment(contextBuilder, singleNameReference)
					? coreFactory.<T>createVariableWrite() : coreFactory.<T>createVariableRead();
		} else if (variable instanceof CtField) {
			variableReference = variable.getReference();
			variableAccess = isLhsAssignment(contextBuilder, singleNameReference)
					? coreFactory.<T>createFieldWrite() : coreFactory.<T>createFieldRead();
		} else { // CtLocalVariable, CtCatchVariable, ...
			variableReference = variable.getReference();
			variableAccess = isLhsAssignment(contextBuilder, singleNameReference)
					? coreFactory.<T>createVariableWrite() : coreFactory.<T>createVariableRead();
		}
		variableReference.setSimpleName(name);
		variableReference.setPosition(positionBuilder.buildPosition(
				singleNameReference.sourceStart(), singleNameReference.sourceEnd()));
		variableAccess.setVariable(variableReference);
		return variableAccess;
	}
