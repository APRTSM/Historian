        private Class superClassOf(Class currentExploredClass) {
            registerTypeParametersOn(clazz.getTypeParameters());
			Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return (Class) genericSuperclass;
        }
