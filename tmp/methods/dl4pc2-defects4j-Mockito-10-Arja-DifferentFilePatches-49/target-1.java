        private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
            registerTypeVariablesOn(clazz.getGenericSuperclass());
			registerTypeParametersOn(clazz.getTypeParameters());
            registerTypeVariablesOn(clazz.getGenericSuperclass());
            for (Type genericInterface : clazz.getGenericInterfaces()) {
                registerTypeVariablesOn(genericInterface);
            }
        }
        private Class superClassOf(Class currentExploredClass) {
            Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return Object.class;
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
