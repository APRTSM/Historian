    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumConverter)) return false;
        EnumConverter<?> that = (EnumConverter<?>) o;
        return Objects.equals(enumType, that.enumType);
    }
    public EnumConverter(Class<T> enumType) {
        if (!Enum.class.isAssignableFrom(enumType)) {
            throw new IllegalArgumentException("Not an Enum: " + enumType.getName());
        }
        this.enumType = Objects.requireNonNull(enumType);
        try {
            this.factory = enumType.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            throw new ConfigException("Uncovertible enum type without createValue method found, please provide a custom " +
                    "PropertyConverter for: " + enumType.getName());
        }
    }
    public T convert(String value, ConversionContext ctx) {
        ctx.addSupportedFormats(getClass(),"<enumValue>");
        try {
            return (T) factory.invoke(null, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOG.log(Level.FINEST, "Invalid enum createValue '" + value + "' for " + enumType.getName(), e);
        }
        try {
            return (T) factory.invoke(null, value.toUpperCase(Locale.ENGLISH));
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOG.log(Level.FINEST, "Invalid enum createValue '" + value + "' for " + enumType.getName(), e);
        }
        return null;
    }
