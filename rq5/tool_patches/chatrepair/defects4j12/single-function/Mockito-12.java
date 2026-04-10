public Class<?> getGenericType(Field field) {        
    Type generic = field.getGenericType();
    if (generic != null && generic instanceof ParameterizedType) {
        Type actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
        if (actual instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actual).getRawType();
        } else {
            return (Class<?>) actual;
        }
    }
    
    return Object.class;
}
