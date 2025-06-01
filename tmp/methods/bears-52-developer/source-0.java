	protected void visitSuperClasses(CtTypeReference<?> superTypeRef, CtConsumer<Object> outputConsumer, boolean includingInterfaces) {
		if (Object.class.getName().equals(superTypeRef.getQualifiedName())) {
			//java.lang.Object has no interface or super classes
			return;
		}
		if (includingInterfaces) {
			visitSuperInterfaces(superTypeRef, outputConsumer);
			if (query.isTerminated()) {
				return;
			}
		}
		CtTypeReference<?> superClassRef = superTypeRef.getSuperclass();
		if (superClassRef == null) {
			CtType<?> superType;
			try {
				superType = superTypeRef.getTypeDeclaration();
			} catch (SpoonClassNotFoundException e) {
				if (failOnClassNotFound) {
					throw e;
				}
				return;
			}
			if (superType instanceof CtClass) {
				// only CtCLasses extend object, so visit Object too
				superClassRef = superTypeRef.getFactory().Type().OBJECT;
			} else {
				return;
			}
		}
		ScanningMode mode = enter(superClassRef);
		if (mode == SKIP_ALL) {
			return;
		}
		sendResult(superClassRef, outputConsumer);
		if (mode == NORMAL && query.isTerminated() == false) {
			visitSuperClasses(superClassRef, outputConsumer, includingInterfaces);
		}
		exit(superClassRef);
	}
	public void apply(CtTypeInformation input, CtConsumer<Object> outputConsumer) {
		CtTypeReference<?> typeRef;
		if (input instanceof CtType) {
			typeRef = ((CtType<?>) input).getReference();
		} else {
			typeRef = (CtTypeReference<?>) input;
		}
		ScanningMode mode = enter(typeRef);
		if (mode == SKIP_ALL) {
			//listener decided to not visit that input. Finish
			return;
		}
		if (includingSelf) {
			sendResult(typeRef, outputConsumer);
			if (query.isTerminated()) {
				mode = SKIP_CHILDREN;
			}
		}
		if (mode == NORMAL) {
			visitSuperClasses(typeRef, outputConsumer, includingInterfaces);
		}
		exit(typeRef);
	}
	protected void visitSuperInterfaces(CtTypeReference<?> type, CtConsumer<Object> outputConsumer) {
		Set<CtTypeReference<?>> superInterfaces;
		try {
			superInterfaces = type.getSuperInterfaces();
		} catch (SpoonClassNotFoundException e) {
			if (failOnClassNotFound) {
				throw e;
			}
			Launcher.LOGGER.warn("Cannot load class: " + type.getQualifiedName() + " with class loader "
					+ Thread.currentThread().getContextClassLoader());
			return;
		}
		for (CtTypeReference<?> ifaceRef : superInterfaces) {
			ScanningMode mode = enter(ifaceRef);
			if (mode == SKIP_ALL) {
				continue;
			}
			sendResult(ifaceRef, outputConsumer);
			if (mode == NORMAL && query.isTerminated() == false) {
				visitSuperInterfaces(ifaceRef, outputConsumer);
			}
			exit(ifaceRef);
			if (query.isTerminated()) {
				return;
			}
		}
	}
	private ScanningMode enter(CtTypeReference<?> type) {
		return listener == null ? NORMAL : listener.enter(type);
	}
	private void exit(CtTypeReference<?> type) {
		if (listener != null) {
			listener.exit(type);
		}
	}
