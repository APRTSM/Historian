        private Class superClassOf(Class currentExploredClass) {
            registerTypeVariablesOn(clazz.getGenericSuperclass());
			Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return (Class) genericSuperclass;
        }
