        private Class superClassOf(Class currentExploredClass) {
            Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            for (Type genericInterface : clazz.getGenericInterfaces()) {
				registerTypeVariablesOn(genericInterface);
			}
			return (Class) genericSuperclass;
        }
