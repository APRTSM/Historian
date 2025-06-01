	private boolean declaringTypeIsLocalOrImported(CtTypeReference declaringType) {
		if (declaringType != null) {
			if (isImportedInClassImports(declaringType)) {
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
	protected boolean classNamePresentInJavaLang(CtTypeReference<?> ref) {
		Boolean presentInJavaLang = namesPresentInJavaLang.get(ref.getSimpleName());
		if (presentInJavaLang == null) {
			// The following procedure of determining if the handle is present in Java Lang or
			// not produces "false positives" if the analyzed source complianceLevel is > 6.
			// For example, it reports that FunctionalInterface is present in java.lang even
			// for compliance levels 6, 7. But this is not considered a bad thing, in opposite,
			// it makes generated code a little more compatible with future versions of Java.
			if (namesPresentInJavaLang8.contains(ref.getSimpleName())
					|| namesPresentInJavaLang9.contains(ref.getSimpleName())) {
				presentInJavaLang = true;
			} else {
				// Assuming Spoon's own runtime environment is Java 7+
				try {
					Class.forName("java.lang." + ref.getSimpleName());
					presentInJavaLang = true;
				} catch (ClassNotFoundException e) {
					presentInJavaLang = false;
				}
			}
			namesPresentInJavaLang.put(ref.getSimpleName(), presentInJavaLang);
		}
		return presentInJavaLang;
	}
	private CtClass getParentClass(CtReference ref) {
		CtElement parent = ref.getParent();

		while (parent != null && !(parent instanceof CtClass)) {
			parent = parent.getParent();
		}

		if (parent == null) {
			return null;
		} else {
			return (CtClass) parent;
		}
	}
	private Set<String> lookForLocalVariables(CtElement parent) {
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
	private boolean shouldTypeBeImported(CtReference ref) {
		// we import the targetType by default to simplify and avoid conclict in inner classes
		if (ref.equals(targetType)) {
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
						if (fieldAndMethodsNames.contains(qualifiedNameTokens.getLast()) || localVariablesOfBlock.contains(qualifiedNameTokens.getLast())) {
							return true;
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
