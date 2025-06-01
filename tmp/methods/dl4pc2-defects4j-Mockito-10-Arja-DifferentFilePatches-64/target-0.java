        private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
            registerTypeParametersOn(clazz.getTypeParameters());
            registerTypeVariablesOn(clazz.getGenericSuperclass());
            registerTypeParametersOn(clazz.getTypeParameters());
			for (Type genericInterface : clazz.getGenericInterfaces()) {
                registerTypeVariablesOn(genericInterface);
            }
        }
