	boolean shouldUseReflectionEntityInstantiator(PersistentEntity<?, ?> entity) {

		Class<?> type = entity.getType();

		if (type.isInterface() //
				|| type.isArray() //
				|| Modifier.isPrivate(type.getModifiers()) //
				|| (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) //
				|| ClassUtils.isCglibProxyClass(type)) { //
			return true;
		}

		PreferredConstructor<?, ?> persistenceConstructor = entity.getPersistenceConstructor();
		if (persistenceConstructor == null || Modifier.isPrivate(persistenceConstructor.getConstructor().getModifiers())) {
			return true;
		}

		if (!ClassUtils.isPresent(ObjectInstantiator.class.getName(), type.getClassLoader())) {
			return true;
		}

		return false;
	}
		private static void visitKotlinCopy(PersistentEntity<?, ?> entity, PersistentProperty<?> property, MethodVisitor mv,
				String internalClassName) {

			KotlinCopyMethod kotlinCopyMethod = KotlinCopyMethod.findCopyMethod(entity.getType())
					.orElseThrow(() -> new IllegalStateException(
							String.format("No usable .copy(…) method found in entity %s", entity.getType().getName())));

			// this. <- for later PUTFIELD
			mv.visitVarInsn(ALOAD, 0);

			if (kotlinCopyMethod.shouldUsePublicCopyMethod(entity)) {

				// PersonWithId.copy$(value)
				mv.visitVarInsn(ALOAD, 3);
				mv.visitVarInsn(ALOAD, 2);

				visitInvokeMethodSingleArg(mv, kotlinCopyMethod.getPublicCopyMethod());
			} else {

				Method copy = kotlinCopyMethod.getSyntheticCopyMethod();
				Class<?>[] parameterTypes = copy.getParameterTypes();

				// PersonWithId.copy$default..(bean, object, MASK, null)
				mv.visitVarInsn(ALOAD, 3);

				KotlinCopyByProperty copyByProperty = kotlinCopyMethod.forProperty(property)
						.orElseThrow(() -> new IllegalStateException(
								String.format("No usable .copy(…) method found for property %s", property)));

				for (int i = 1; i < kotlinCopyMethod.getParameterCount(); i++) {

					if (copyByProperty.getParameterPosition() == i) {

						mv.visitVarInsn(ALOAD, 2);

						mv.visitTypeInsn(CHECKCAST, Type.getInternalName(autoboxType(parameterTypes[i])));
						autoboxIfNeeded(autoboxType(parameterTypes[i]), parameterTypes[i], mv);

						continue;
					}

					visitDefaultValue(parameterTypes[i], mv);
				}

				copyByProperty.getDefaultMask().forEach(i -> {
					mv.visitIntInsn(Opcodes.SIPUSH, i);
				});

				mv.visitInsn(Opcodes.ACONST_NULL);

				int invokeOpCode = getInvokeOp(copy, false);

				mv.visitMethodInsn(invokeOpCode, Type.getInternalName(copy.getDeclaringClass()), copy.getName(),
						getArgumentSignature(copy), false);
			}

			mv.visitFieldInsn(PUTFIELD, internalClassName, BEAN_FIELD, getAccessibleTypeReferenceName(entity));
		}
	private static boolean isTypeInjectable(PersistentEntity<?, ?> entity) {

		Class<?> type = entity.getType();
		return type.getClassLoader() != null
				&& (type.getPackage() == null || !type.getPackage().getName().startsWith("java"))
				&& ClassUtils.isPresent(PersistentPropertyAccessor.class.getName(), type.getClassLoader())
				&& ClassUtils.isPresent(Assert.class.getName(), type.getClassLoader());
	}
