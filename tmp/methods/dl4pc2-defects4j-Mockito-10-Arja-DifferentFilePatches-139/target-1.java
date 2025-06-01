    public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        return new ReturnsEmptyValues().answer(invocation);
    }
        private Class superClassOf(Class currentExploredClass) {
            Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
				Type rawType = ((ParameterizedType) genericSuperclass)
						.getRawType();
				return (Class) rawType;
			}
			if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return (Class) genericSuperclass;
        }
