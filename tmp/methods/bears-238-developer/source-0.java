    private boolean fieldAcceptable(Field field) {
        return field.getType().isPrimitive() || field.getType().isArray() || ignoredNames.contains(field.getName());
    }
