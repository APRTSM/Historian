	private boolean declaringTypeIsLocalOrImported(CtTypeReference declaringType) {
		if (declaringType != null) {
			if (isImportedInClassImports(declaringType) || classNamePresentInJavaLang(declaringType)) {
				return true;
			}

			if (!isTypeInCollision(declaringType, false) && addClassImport(declaringType)) {
				return true;
			}

			while (declaringType != null) {
				if (declaringType.equals(targetType)) {
					return true;
				}
				declaringType = declaringType.getDeclaringType();
			}

		}
		return false;
	}
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

			Set<String> localVariablesOfBlock = new HashSet<>();

			if (parent instanceof CtField) {
				this.fieldAndMethodsNames.add(((CtField) parent).getSimpleName());
			} else if (parent instanceof CtMethod) {
				this.fieldAndMethodsNames.add(((CtMethod) parent).getSimpleName());
			} else {
				localVariablesOfBlock = this.lookForLocalVariables(parent);
			}

			while (!(parent instanceof CtPackage)) {
				if ((parent instanceof CtFieldReference) || (parent instanceof CtExecutableReference)) {
					CtReference parentType = (CtReference) parent;
					LinkedList<String> qualifiedNameTokens = new LinkedList<>();

					// we don't want to test the current ref name, as we risk to create field import and make autoreference
					if (parentType != parent) {
						qualifiedNameTokens.add(parentType.getSimpleName());
					}

					CtTypeReference typeReference;
					if (parent instanceof CtFieldReference) {
						typeReference = ((CtFieldReference) parent).getDeclaringType();
					} else {
						typeReference = ((CtExecutableReference) parent).getDeclaringType();
					}

					if (typeReference != null) {
						qualifiedNameTokens.add(typeReference.getSimpleName());

						if (typeReference.getPackage() != null) {
							CtPackage ctPackage = typeReference.getPackage().getDeclaration();

							while (ctPackage != null) {
								qualifiedNameTokens.add(ctPackage.getSimpleName());

								CtElement packParent = ctPackage.getParent();
								if (packParent.getParent() != null && !((CtPackage) packParent).getSimpleName().equals(CtPackage.TOP_LEVEL_PACKAGE_NAME)) {
									ctPackage = (CtPackage) packParent;
								} else {
									ctPackage = null;
								}
							}
						}
					}
					if (!qualifiedNameTokens.isEmpty()) {
						// qualified name token are ordered in the reverse order
						// if the first package name is a variable name somewhere, it could lead to a collision
						if (fieldAndMethodsNames.contains(qualifiedNameTokens.getLast()) || localVariablesOfBlock.contains(qualifiedNameTokens.getLast())) {
							qualifiedNameTokens.removeLast();

							if (fqnMode) {
								return true;
							} else {
								// but if the other package names are not a variable name, it's ok to import
								for (int i = qualifiedNameTokens.size() - 1; i > 0; i--) {
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
	protected Set<String> lookForLocalVariables(CtElement parent) {
		Set<String> result = new HashSet<>();

		// try to get the block container
		// if the first container is the class, then we are not in a block and we can quit now.
		while (parent != null && !(parent instanceof CtBlock)) {
			if (parent instanceof CtClass) {
				return result;
			}
			parent = parent.getParent();
		}

		if (parent != null) {
			CtBlock block = (CtBlock) parent;
			boolean innerClass = false;

			// now we have the first container block, we want to check if we're not in an inner class
			while (parent != null && !(parent instanceof CtClass)) {
				parent = parent.getParent();
			}

			if (parent != null) {
				// uhoh it's not a package as a parent, we must in an inner block:
				// let's find the last block BEFORE the class call: some collision could occur because of variables defined in that block
				if (!(parent.getParent() instanceof CtPackage)) {
					while (parent != null && !(parent instanceof CtBlock)) {
						parent = parent.getParent();
					}

					if (parent != null) {
						block = (CtBlock) parent;
					}
				}
			}

			AccessibleVariablesFinder avf = new AccessibleVariablesFinder(block);
			List<CtVariable> variables = avf.find();

			for (CtVariable variable : variables) {
				result.add(variable.getSimpleName());
			}
		}

		return result;
	}
