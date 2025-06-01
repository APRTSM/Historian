        private Class superClassOf(Class currentExploredClass) {
            Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            registerTypeParametersOn(clazz.getTypeParameters());
			if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return Object.class;
        }
