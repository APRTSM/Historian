		public ScanningMode enter(CtTypeReference<?> typeRef, boolean isClass) {
			return enter((CtElement) typeRef);
		}
	public void apply(CtTypeInformation input, CtConsumer<Object> outputConsumer) {
		CtTypeReference<?> typeRef;
		CtType<?> type;
		//detect whether input is a class or something else (e.g. interface)
		boolean isClass;
		if (input instanceof CtType) {
			type = (CtType<?>) input;
			typeRef = type.getReference();
		} else {
			typeRef = (CtTypeReference<?>) input;
			try {
				type = typeRef.getTypeDeclaration();
			} catch (SpoonClassNotFoundException e) {
				if (typeRef.getFactory().getEnvironment().getNoClasspath() == false) {
					throw e;
				}
				type = null;
			}
		}
		//if the type is unknown, than we expect it is interface, otherwise we would visit java.lang.Object too, even for interfaces
		isClass = type == null ? false : (type instanceof CtClass);
		if (isClass == false && includingInterfaces == false) {
			//the input is interface, but this scanner should visit only interfaces. Finish
			return;
		}
		ScanningMode mode = enter(typeRef, isClass);
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
			if (isClass == false) {
				visitSuperInterfaces(typeRef, outputConsumer);
			} else {
				//call visitSuperClasses only for input of type class. The contract of visitSuperClasses requires that
				visitSuperClasses(typeRef, outputConsumer, includingInterfaces);
			}
		}
		exit(typeRef, isClass);
	}
		public void exit(CtTypeReference<?> typeRef, boolean isClass) {
			exit((CtElement) typeRef);
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
			ScanningMode mode = enter(ifaceRef, true);
			if (mode == SKIP_ALL) {
				continue;
			}
			sendResult(ifaceRef, outputConsumer);
			if (mode == NORMAL && query.isTerminated() == false) {
				visitSuperInterfaces(ifaceRef, outputConsumer);
			}
			exit(ifaceRef, true);
			if (query.isTerminated()) {
				return;
			}
		}
	}
	private ScanningMode enter(CtTypeReference<?> type, boolean isClass) {
		if (listener == null) {
			return NORMAL;
		}
		if (listener instanceof Listener) {
			Listener typeListener = (Listener) listener;
			return typeListener.enter(type, isClass);
		}
		return listener.enter(type);
	}
		public void exit(CtElement element) {
		}
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
			//only CtClasses extend object,
			//this method is called only for classes (not for interfaces) so we know we can visit java.lang.Object now too
			superClassRef = superTypeRef.getFactory().Type().OBJECT;
		}
		ScanningMode mode = enter(superClassRef, false);
		if (mode == SKIP_ALL) {
			return;
		}
		sendResult(superClassRef, outputConsumer);
		if (mode == NORMAL && query.isTerminated() == false) {
			visitSuperClasses(superClassRef, outputConsumer, includingInterfaces);
		}
		exit(superClassRef, false);
	}
		public ScanningMode enter(CtElement element) {
			return ScanningMode.NORMAL;
		}
	private void exit(CtTypeReference<?> type, boolean isClass) {
		if (listener != null) {
			if (listener instanceof Listener) {
				((Listener) listener).exit(type, isClass);
			} else {
				listener.exit(type);
			}
		}
	}
