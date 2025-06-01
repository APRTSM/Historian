	public <T> void visitCtFieldReference(CtFieldReference<T> reference) {
		enter(reference);
		if (reference.isStatic()) {
			if (!addFieldImport(reference)) {
				scan(reference.getDeclaringType());
			}
		} else {
			scan(reference.getDeclaringType());
		}
		exit(reference);
	}
	public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
		if (!(reference instanceof CtArrayTypeReference)) {
			if (reference.getDeclaringType() == null) {
				addClassImport(reference);
			} else {
				addClassImport(reference.getAccessType());
			}
		}
		super.visitCtTypeReference(reference);

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
	protected boolean addClassImport(CtTypeReference<?> ref) {
		this.exploredReferences.add(ref);
		if (ref == null) {
			return false;
		}

		if (targetType != null && targetType.getSimpleName().equals(ref.getSimpleName()) && !targetType.equals(ref)) {
			return false;
		}
		if (classImports.containsKey(ref.getSimpleName())) {
			return isImportedInClassImports(ref);
		}
		// don't import unnamed package elements
		if (ref.getPackage() == null || ref.getPackage().isUnnamedPackage()) {
			return false;
		}
		if (ref.getPackage().getSimpleName().equals("java.lang")) {
			if (classNamePresentInJavaLang(ref)) {
				// Don't import class with names clashing with some classes present in java.lang,
				// because it leads to undecidability and compilation errors. I. e. always leave
				// com.mycompany.String fully-qualified.
				return false;
			}
		}
		if (targetType != null && targetType.canAccess(ref) == false) {
			//ref type is not visible in targetType we must not add import for it, java compiler would fail on that.
			return false;
		}

		if (this.isThereAnotherClassWithSameNameInAnotherPackage(ref)) {
			return false;
		}

		// we want to be sure that we are not importing a class because a static field or method we already imported
		// moreover we make exception for same package classes to avoid problems in FQN mode

		if (targetType != null) {
			try {
				CtElement parent = ref.getParent();
				if (parent != null) {
					parent = parent.getParent();
					if (parent != null) {
						if ((parent instanceof CtFieldAccess) || (parent instanceof CtExecutable) || (parent instanceof CtInvocation)) {

							CtTypeReference declaringType;
							CtReference reference;
							CtPackageReference pack = targetType.getPackage();
							if (parent instanceof CtFieldAccess) {
								CtFieldAccess field = (CtFieldAccess) parent;
								CtFieldReference localReference = field.getVariable();
								declaringType = localReference.getDeclaringType();
								reference = localReference;
							} else if (parent instanceof CtExecutable) {
								CtExecutable exec = (CtExecutable) parent;
								CtExecutableReference localReference = exec.getReference();
								declaringType = localReference.getDeclaringType();
								reference = localReference;
							} else if (parent instanceof CtInvocation) {
								CtInvocation invo = (CtInvocation) parent;
								CtExecutableReference localReference = invo.getExecutable();
								declaringType = localReference.getDeclaringType();
								reference = localReference;
							} else {
								declaringType = null;
								reference = null;
							}

							if (reference != null && isImported(reference)) {
								// if we are in the **same** package we do the import for test with method isImported
								if (declaringType != null) {
									if (declaringType.getPackage() != null && !declaringType.getPackage().isUnnamedPackage()) {
										// ignore java.lang package
										if (!declaringType.getPackage().getSimpleName().equals("java.lang")) {
											// ignore type in same package
											if (declaringType.getPackage().getSimpleName()
													.equals(pack.getSimpleName())) {
												classImports.put(ref.getSimpleName(), ref);
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			} catch (ParentNotInitializedException e) {
			}
			CtPackageReference pack = targetType.getPackage();
			if (ref.getPackage() != null && !ref.getPackage().isUnnamedPackage()) {
				// ignore java.lang package
				if (!ref.getPackage().getSimpleName().equals("java.lang")) {
					// ignore type in same package
					if (ref.getPackage().getSimpleName()
							.equals(pack.getSimpleName())) {
						return false;
					}
				}
			}
		}

		classImports.put(ref.getSimpleName(), ref);
		return true;
	}
