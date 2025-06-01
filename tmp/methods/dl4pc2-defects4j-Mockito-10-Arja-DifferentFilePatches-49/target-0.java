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
