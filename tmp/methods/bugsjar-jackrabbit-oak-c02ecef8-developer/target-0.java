    public PropertyBuilder<T> assignFrom(PropertyState property) {
        if (property != null) {
            setName(property.getName());
            if (property.isArray()) {
                isArray = true;
                if (type == Type.DATE) {
                    setValues((Iterable<T>) property.getValue(Type.STRINGS));
                }
                else {
                    setValues((Iterable<T>) property.getValue(type.getArrayType()));
                }
            }
            else {
                isArray = false;
                if (type == Type.DATE) {
                    setValue((T) property.getValue(Type.STRING));
                }
                else {
                    setValue(property.getValue(type));
                }
            }
        }
        return this;
    }
