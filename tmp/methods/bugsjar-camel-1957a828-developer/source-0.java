    private void removeAllAbstractMethods(List<MethodInfo> methods) {
        Iterator<MethodInfo> it = methods.iterator();
        while (it.hasNext()) {
            MethodInfo info = it.next();
            if (Modifier.isAbstract(info.getMethod().getModifiers())) {
                // we cannot invoke an abstract method
                it.remove();
            }
        }
    }
