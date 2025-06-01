        private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
            registerTypeVariablesOn(clazz.getGenericSuperclass());
			registerTypeParametersOn(clazz.getTypeParameters());
            registerTypeVariablesOn(clazz.getGenericSuperclass());
            for (Type genericInterface : clazz.getGenericInterfaces()) {
                registerTypeVariablesOn(genericInterface);
            }
        }
