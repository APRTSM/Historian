    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        if (obj == null)
            return null;
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
