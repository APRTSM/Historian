    public void validateSerializable(Class classToMock, boolean serializable) {
        // We can't catch all the errors with this piece of code
        // Having a **superclass that do not implements Serializable** might fail as well when serialized
        // Though it might prevent issues when mockito is mocking a class without superclass.
        if(serializable
                && !classToMock.isInterface()
                && !(Serializable.class.isAssignableFrom(classToMock))
                && Constructors.noArgConstructorOf(classToMock) == null
                ) {
            new Reporter().serializableWontWorkForObjectsThatDontImplementSerializable(classToMock);
        }
    }
    protected void registerTypeParametersOn(TypeVariable[] typeParameters) {
        for (TypeVariable typeVariable : typeParameters) {
            registerTypeVariableIfNotPresent(typeVariable);
        }
    }
    public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (!mockitoCore().isTypeMockable(rawType)) {
            return delegate().returnValueFor(rawType);
        }

        return deepStub(invocation, returnTypeGenericMetadata);
    }
