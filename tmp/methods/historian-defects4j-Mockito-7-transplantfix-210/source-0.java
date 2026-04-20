    protected Type getActualTypeArgumentFor(TypeVariable typeParameter) {
        Type type = this.contextualActualTypeParameters.get(typeParameter);
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return getActualTypeArgumentFor(typeVariable);
        }

        return type;
    }
