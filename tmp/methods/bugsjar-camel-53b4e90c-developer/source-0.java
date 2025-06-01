    private void introspect(Class<?> clazz) {
        // get the target clazz as it could potentially have been enhanced by CGLIB etc.
        clazz = getTargetClass(clazz);
        ObjectHelper.notNull(clazz, "clazz", this);

        LOG.trace("Introspecting class: {}", clazz);

        // favor declared methods, and then filter out duplicate interface methods
        List<Method> methods;
        if (Modifier.isPublic(clazz.getModifiers())) {
            LOG.trace("Preferring class methods as class: {} is public accessible", clazz);
            methods = new ArrayList<Method>(Arrays.asList(clazz.getDeclaredMethods()));
        } else {
            LOG.trace("Preferring interface methods as class: {} is not public accessible", clazz);
            methods = getInterfaceMethods(clazz);
            // and then we must add its declared methods as well
            List<Method> extraMethods = Arrays.asList(clazz.getDeclaredMethods());
            methods.addAll(extraMethods);
        }

        // it may have duplicate methods already, even from declared or from interfaces + declared
        Set<Method> overrides = new HashSet<Method>();
        for (Method source : methods) {
            for (Method target : methods) {
                // skip ourselves
                if (ObjectHelper.isOverridingMethod(source, target, true)) {
                    continue;
                }
                // skip duplicates which may be assign compatible (favor keep first added method when duplicate)
                if (ObjectHelper.isOverridingMethod(source, target, false)) {
                    overrides.add(target);
                }
            }
        }
        methods.removeAll(overrides);
        overrides.clear();

        // if we are a public class, then add non duplicate interface classes also
        if (Modifier.isPublic(clazz.getModifiers())) {
            // add additional interface methods
            List<Method> extraMethods = getInterfaceMethods(clazz);
            for (Method target : extraMethods) {
                for (Method source : methods) {
                    if (ObjectHelper.isOverridingMethod(source, target, false)) {
                        overrides.add(target);
                    }
                }
            }
            // remove all the overrides methods
            extraMethods.removeAll(overrides);
            methods.addAll(extraMethods);
        }

        // now introspect the methods and filter non valid methods
        for (Method method : methods) {
            boolean valid = isValidMethod(clazz, method);
            LOG.trace("Method: {} is valid: {}", method, valid);
            if (valid) {
                introspect(clazz, method);
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            introspect(superclass);
        }
    }
