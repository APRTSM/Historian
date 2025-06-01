    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = this;
        Object h = src.getTypeHandler();
        if (h != _typeHandler) {
            type = type.withTypeHandler(h);
        }
        h = src.getValueHandler();
        if (h != _valueHandler) {
            type = type.withValueHandler(h);
        }
        return type;
    }
    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = super.withHandlersFrom(src);
        JavaType srcCt = src.getContentType();
        if (srcCt != null) {
            JavaType ct = _elementType.withHandlersFrom(srcCt);
            if (ct != _elementType) {
                type = type.withContentType(ct);
            }
        }
        return type;
    }
