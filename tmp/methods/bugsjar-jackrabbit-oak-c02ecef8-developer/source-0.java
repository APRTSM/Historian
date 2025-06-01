    public PropertyBuilder<T> assignFrom(PropertyState property) {
        if (property != null) {
            setName(property.getName());
            if (property.isArray()) {
                isArray = true;
                setValues((Iterable<T>) property.getValue(type.getArrayType()));
            }
            else {
                isArray = false;
                setValue(property.getValue(type));
            }
        }
        return this;
    }
