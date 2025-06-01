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
    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = super.withHandlersFrom(src);
        JavaType srcKeyType = src.getKeyType();
        // "withKeyType()" not part of JavaType, hence must verify:
        if (type instanceof MapLikeType) {
            if (srcKeyType != null) {
                JavaType ct = _keyType.withHandlersFrom(srcKeyType);
                if (ct != _keyType) {
                    type = ((MapLikeType) type).withKeyType(ct);
                }
            }
        }
        JavaType srcCt = src.getContentType();
        if (srcCt != null) {
            JavaType ct = _valueType.withHandlersFrom(srcCt);
            if (ct != _valueType) {
                type = type.withContentType(ct);
            }
        }
        return type;
    }
