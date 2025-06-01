	public List<CtTypeReference<?>> resolveActualTypeArgumentsOf(CtTypeReference<?> typeRef) {
		final String typeQualifiedName = typeRef.getQualifiedName();
		List<CtTypeReference<?>> args = typeToArguments.get(typeQualifiedName);
		if (args != null) {
			//the actual type arguments of `type` are already resolved
			return args;
		}
		//resolve hierarchy of enclosing class first.
		CtTypeReference<?> enclosingTypeRef = getEnclosingType(typeRef);
		if (enclosingTypeRef != null) {
			if (enclosingClassTypingContext == null) {
				return null;
			}
			//`type` is inner class. Resolve it's enclosing class arguments first
			if (enclosingClassTypingContext.resolveActualTypeArgumentsOf(enclosingTypeRef) == null) {
				return null;
			}
		}
		/*
		 * the `type` is either top level, static or resolved inner class.
		 * So it has no parent actual type arguments or they are resolved now
		 */
		/*
		 * detect where to start/continue with resolving of super classes and super interfaces
		 * to found actual type arguments of input `type`
		 */
		if (lastResolvedSuperclass == null) {
			/*
			 * whole super inheritance hierarchy was already resolved for this level.
			 * It means that `type` is not a super type of `scope` on the level `level`
			 */
			return null;
		}
		final HierarchyListener listener = new HierarchyListener(getVisitedSet());
		/*
		 * visit super inheritance class hierarchy of lastResolve type of level of `type` to found it's actual type arguments.
		 */
		((CtElement) lastResolvedSuperclass).map(new SuperInheritanceHierarchyFunction()
				.includingSelf(false)
				.returnTypeReferences(true)
				.setListener(listener))
		.forEach(new CtConsumer<CtTypeReference<?>>() {
			@Override
			public void accept(CtTypeReference<?> typeRef) {
				/*
				 * typeRef is a reference from sub type to super type.
				 * It contains actual type arguments in scope of sub type,
				 * which are going to be substituted as arguments to formal type parameters of super type
				 */
				String superTypeQualifiedName = typeRef.getQualifiedName();
				List<CtTypeReference<?>> superTypeActualTypeArgumentsResolvedFromSubType = resolveTypeParameters(typeRef.getActualTypeArguments());
				//Remember actual type arguments of `type`
				typeToArguments.put(superTypeQualifiedName, superTypeActualTypeArgumentsResolvedFromSubType);
				if (typeQualifiedName.equals(superTypeQualifiedName)) {
					/*
					 * we have found actual type arguments of input `type`
					 * We can finish. But only after all interfaces of last visited class are processed too
					 */
					listener.foundArguments = superTypeActualTypeArgumentsResolvedFromSubType;
				}
			}
		});
		return listener.foundArguments;
	}
		public ScanningMode enter(CtTypeReference<?> typeRef, boolean isClass) {
			ScanningMode mode = super.enter(typeRef);
			if (mode == ScanningMode.SKIP_ALL) {
				//this interface was already visited. Do not visit it again
				return mode;
			}
			if (isClass) {
				if (foundArguments != null) {
					//we have found result then we can finish before entering super class. All interfaces of found type should be still visited
					//skip before super class (and it's interfaces) of found type is visited
					return ScanningMode.SKIP_ALL;
				}
				/*
				 * we are visiting class (not interface)
				 * Remember that, so we can continue at this place if needed.
				 * If we enter class, then this listener assures that that class and all it's not yet visited interfaces are visited
				 */
				lastResolvedSuperclass = typeRef;
			}
			//this type was not visited yet. Visit it normally
			return ScanningMode.NORMAL;
		}
