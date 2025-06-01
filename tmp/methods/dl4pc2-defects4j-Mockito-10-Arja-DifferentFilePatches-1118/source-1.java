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
    private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
        InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
        InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
            if (container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
                return stubbedInvocationMatcher.answer(invocation);
            }
        }

        // record deep stub answer
        return recordDeepStubAnswer(
                newDeepStubMock(returnTypeGenericMetadata),
                container
        );
    }
