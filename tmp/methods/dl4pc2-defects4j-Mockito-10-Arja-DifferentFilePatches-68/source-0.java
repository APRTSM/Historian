        private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
            registerTypeParametersOn(clazz.getTypeParameters());
            registerTypeVariablesOn(clazz.getGenericSuperclass());
            for (Type genericInterface : clazz.getGenericInterfaces()) {
                registerTypeVariablesOn(genericInterface);
            }
        }
