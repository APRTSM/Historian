    public GenericMetadataSupport resolveGenericReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        // logger.log("Method '" + method.toGenericString() + "' has return type : " + genericReturnType.getClass().getInterfaces()[0].getSimpleName() + " : " + genericReturnType);

        if (genericReturnType instanceof Class) {
            if (genericReturnType instanceof ParameterizedType) {
				return new ParameterizedReturnType(this,
						method.getTypeParameters(),
						(ParameterizedType) method.getGenericReturnType());
			}
			return new NotGenericReturnTypeSupport(genericReturnType);
        }
        if (genericReturnType instanceof ParameterizedType) {
            return new ParameterizedReturnType(this, method.getTypeParameters(), (ParameterizedType) method.getGenericReturnType());
        }
        if (genericReturnType instanceof TypeVariable) {
            return new TypeVariableReturnType(this, method.getTypeParameters(), (TypeVariable) genericReturnType);
        }

        throw new MockitoException("Ouch, it shouldn't happen, type '" + genericReturnType.getClass().getCanonicalName() + "' on method : '" + method.toGenericString() + "' is not supported : " + genericReturnType);
    }
    protected void registerTypeParametersOn(TypeVariable[] typeParameters) {
        for (TypeVariable typeVariable : typeParameters) {
			registerTypeVariableIfNotPresent(typeVariable);
		}
		for (TypeVariable typeVariable : typeParameters) {
            registerTypeVariableIfNotPresent(typeVariable);
        }
    }
    public void validateSerializable(Class classToMock, boolean serializable) {
        // We can't catch all the errors with this piece of code
        // Having a **superclass that do not implements Serializable** might fail as well when serialized
        // Though it might prevent issues when mockito is mocking a class without superclass.
        if(serializable
                && !classToMock.isInterface()
                && !(Serializable.class.isAssignableFrom(classToMock))
                && Constructors.noArgConstructorOf(classToMock) == null
                ) {
        }
    }
