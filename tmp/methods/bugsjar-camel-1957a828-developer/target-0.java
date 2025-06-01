    private void removeAllAbstractMethods(List<MethodInfo> methods) {
        Iterator<MethodInfo> it = methods.iterator();
        while (it.hasNext()) {
            MethodInfo info = it.next();
            // if the class is an interface then keep the method
            boolean isFromInterface = Modifier.isInterface(info.getMethod().getDeclaringClass().getModifiers());
            if (!isFromInterface && Modifier.isAbstract(info.getMethod().getModifiers())) {
                // we cannot invoke an abstract method
                it.remove();
            }
        }
    }
