	<T> CtVariableAccess<T> createVariableAccessNoClasspath(SingleNameReference singleNameReference) {
		final TypeFactory typeFactory = jdtTreeBuilder.getFactory().Type();
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

				// Since the given parameter has not been declared in a lambda expression it must
				// have been declared by a method/constructor.
				final CtExecutable executable = (CtExecutable) variable.getParent();

				// create list of executable's parameter types
				final List<CtTypeReference<?>> parameterTypesOfExecutable = new ArrayList<>();
				@SuppressWarnings("unchecked")
				final List<CtParameter<?>> parametersOfExecutable = executable.getParameters();
				for (CtParameter<?> parameter : parametersOfExecutable) {
					if (parameter.getType() != null) {
						parameterTypesOfExecutable.add(parameter.getType().clone());
					} else {
						// it's the best match :(
						parameterTypesOfExecutable.add(typeFactory.OBJECT);
					}
				}

				// find executable's corresponding jdt element
				AbstractMethodDeclaration executableJDT = null;
				for (final ASTPair astPair : contextBuilder.stack) {
					if (astPair.element == executable) {
						executableJDT = (AbstractMethodDeclaration) astPair.node;
					}
				}
				assert executableJDT != null;

				// create a reference to executable's declaring class
				final CtTypeReference declaringReferenceOfExecutable =
						// `binding` may be null for anonymous classes which means we have to
						// create an 'empty' type reference since we have no further information
						// available
						executableJDT.binding == null ? coreFactory.createTypeReference()
								: referenceBuilder.getTypeReference(
										executableJDT.binding.declaringClass);

				// If executable is a constructor, `executable.getType()` returns null since the
				// parent is not available yet. Fortunately, however, the return type of a
				// constructor is its declaring class which, in our case, is already available with
				// declaringReferenceOfExecutable.
				CtTypeReference executableTypeReference = executable instanceof CtConstructor
						// IMPORTANT: Create a clone of the type reference (rt) if retrieved by
						// other AST elements as `executableFactory.createReference` (see below)
						// indirectly sets the parent of `rt` and, thus, may break the AST!
						? declaringReferenceOfExecutable.clone()
						: executable.getType().clone();

				// create a reference to the executable of the currently processed parameter
				// reference
				@SuppressWarnings("unchecked")
				final CtExecutableReference executableReference =
						executableFactory.createReference(
								declaringReferenceOfExecutable,
								executableTypeReference,
								executable.getSimpleName(),
								parameterTypesOfExecutable);

				// finally, we can set the executable reference...
				parameterReference.setDeclaringExecutable(executableReference);
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
