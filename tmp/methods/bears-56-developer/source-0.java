	protected boolean isTypeInCollision(CtReference ref, boolean fqnMode) {
		if (targetType.getSimpleName().equals(ref.getSimpleName()) && !targetType.equals(ref)) {
			return true;
		}

		try {
			CtElement parent;
			if (ref instanceof CtTypeReference) {
				parent = ref.getParent();
			} else {
				parent = ref;
			}

			// in that case we are trying to import a type because of a literal we are scanning
			// i.e. a string, an int, etc.
			if (parent instanceof CtLiteral) {
				return false;
			}

			Set<String> localVariablesOfBlock = new HashSet<>();

			if (parent instanceof CtField) {
				this.fieldAndMethodsNames.add(((CtField) parent).getSimpleName());
			} else if (parent instanceof CtMethod) {
				this.fieldAndMethodsNames.add(((CtMethod) parent).getSimpleName());
			} else {
				localVariablesOfBlock = this.lookForLocalVariables(parent);
			}

			while (!(parent instanceof CtPackage)) {
				if ((parent instanceof CtFieldReference) || (parent instanceof CtExecutableReference) || (parent instanceof CtInvocation)) {
					CtReference parentType;
					if (parent instanceof CtInvocation) {
						parentType = ((CtInvocation) parent).getExecutable();
					} else {
						parentType = (CtReference) parent;
					}
					LinkedList<String> qualifiedNameTokens = new LinkedList<>();

					// we don't want to test the current ref name, as we risk to create field import and make autoreference
					if (parentType != parent) {
						qualifiedNameTokens.add(parentType.getSimpleName());
					}

					CtTypeReference typeReference;
					if (parent instanceof CtFieldReference) {
						typeReference = ((CtFieldReference) parent).getDeclaringType();
					} else if (parent instanceof CtExecutableReference) {
						typeReference = ((CtExecutableReference) parent).getDeclaringType();
					} else {
						typeReference = ((CtInvocation) parent).getExecutable().getDeclaringType();
					}

					if (typeReference != null) {
						qualifiedNameTokens.addFirst(typeReference.getSimpleName());

						if (typeReference.getPackage() != null) {
							StringTokenizer token = new StringTokenizer(typeReference.getPackage().getSimpleName(), CtPackage.PACKAGE_SEPARATOR);
							int index = 0;
							while (token.hasMoreElements()) {
								qualifiedNameTokens.add(index, token.nextToken());
								index++;
							}
						}
					}
					if (!qualifiedNameTokens.isEmpty()) {
						// qualified name token are ordered in the reverse order
						// if the first package name is a variable name somewhere, it could lead to a collision
						if (fieldAndMethodsNames.contains(qualifiedNameTokens.getFirst()) || localVariablesOfBlock.contains(qualifiedNameTokens.getFirst())) {
							qualifiedNameTokens.removeFirst();

							if (fqnMode) {
								// in case we are testing a type: we should not import it if its entire name is in collision
								// for example: spoon.Launcher if a field spoon and another one Launcher exists
								if (ref instanceof CtTypeReference) {
									if (qualifiedNameTokens.isEmpty()) {
										return true;
									}
									// but if the other package names are not a variable name, it's ok to import
									for (int i =  0; i < qualifiedNameTokens.size(); i++) {
										String testedToken = qualifiedNameTokens.get(i);
										if (!fieldAndMethodsNames.contains(testedToken) && !localVariablesOfBlock.contains(testedToken)) {
											return true;
										}
									}
									return false;

								// However if it is a static method/field, we always accept to import them in this case
								// It is the last possibility for managing import for us
								} else {
									return true;
								}
							} else {
								// but if the other package names are not a variable name, it's ok to import
								for (int i =  0; i < qualifiedNameTokens.size(); i++) {
									String testedToken = qualifiedNameTokens.get(i);
									if (!fieldAndMethodsNames.contains(testedToken) && !localVariablesOfBlock.contains(testedToken)) {
										return false;
									}
								}
								return true;
							}
						}
					}


				}
				parent = parent.getParent();
			}
		} catch (ParentNotInitializedException e) {
			return false;
		}

		return false;
	}
