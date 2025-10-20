public Object answer(InvocationOnMock invocation) {
    if (methodsGuru.isToString(invocation.getMethod())) {
        Object mock = invocation.getMock();
        MockName name = mockUtil.getMockName(mock);
        if (name.isDefault()) {
            return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
        } else {
            return name.toString();
        }
    } else if (methodsGuru.isCompareToMethod(invocation.getMethod())) {
        // Only for compareTo() method by the Comparable interface
        Object[] args = invocation.getArguments();
        Object mockedObject = invocation.getMock();

        if (args != null && args.length == 1 && args[0] == mockedObject) {
          return 0; // return 0 if compared with itself
        } else {
          return 1; // any other value if not compared with itself
        }
    }
    
    Class<?> returnType = invocation.getMethod().getReturnType();
    return returnValueFor(returnType);
}
