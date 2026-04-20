        private Class superClassOf(Class currentExploredClass) {
            for (Type genericInterface : clazz.getGenericInterfaces()) {
				registerTypeVariablesOn(genericInterface);
			}
			Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return (Class) genericSuperclass;
        }
