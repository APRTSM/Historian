            public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext, MethodDescription instrumentedMethod) {
                if (!instrumentedMethod.isMethod()) {
                    throw new IllegalArgumentException(instrumentedMethod + " does not describe a field getter or setter");
                }
                FieldDescription fieldDescription = fieldLocation.resolve(instrumentedMethod);
                if (!instrumentedType.isAssignableTo(fieldDescription.getDeclaringType().asErasure())) {
                    throw new IllegalStateException(fieldDescription + " is not declared in the hierarchy of " + instrumentedType);
                } else if (!fieldDescription.isAccessibleTo(instrumentedType)) {
                    throw new IllegalStateException("Cannot access " + fieldDescription + " from " + instrumentedType);
                } else if (!fieldDescription.isStatic() && instrumentedMethod.isStatic()) {
                    throw new IllegalStateException("Cannot set instance field " + fieldDescription + " from " + instrumentedMethod);
                }
                StackManipulation implementation, initialization = fieldDescription.isStatic()
                        ? StackManipulation.Trivial.INSTANCE
                        : MethodVariableAccess.loadThis();
                if (!instrumentedMethod.getReturnType().represents(void.class)) {
                    implementation = new StackManipulation.Compound(
                            initialization,
                            FieldAccess.forField(fieldDescription).read(),
                            assigner.assign(fieldDescription.getType(), instrumentedMethod.getReturnType(), typing),
                            MethodReturn.of(instrumentedMethod.getReturnType())
                    );
                } else if (instrumentedMethod.getReturnType().represents(void.class) && instrumentedMethod.getParameters().size() == 1) {
                    if (fieldDescription.isFinal() && instrumentedMethod.isMethod()) {
                        throw new IllegalStateException("Cannot set final field " + fieldDescription + " from " + instrumentedMethod);
                    }
                    implementation = new StackManipulation.Compound(
                            initialization,
                            MethodVariableAccess.load(instrumentedMethod.getParameters().get(0)),
                            assigner.assign(instrumentedMethod.getParameters().get(0).getType(), fieldDescription.getType(), typing),
                            FieldAccess.forField(fieldDescription).write(),
                            MethodReturn.VOID
                    );
                } else {
                    throw new IllegalArgumentException("Method " + implementationContext + " is no bean property");
                }
                if (!implementation.isValid()) {
                    throw new IllegalStateException("Cannot set or get value of " + instrumentedMethod + " using " + fieldDescription);
                }
                return new Size(implementation.apply(methodVisitor, implementationContext).getMaximalSize(), instrumentedMethod.getStackSize());
            }
