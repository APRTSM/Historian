    public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        return new ReturnsEmptyValues().answer(invocation);
    }
