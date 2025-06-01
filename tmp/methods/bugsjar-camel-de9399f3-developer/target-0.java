    public void addTypeConverter(Class<?> toType, Class<?> fromType, TypeConverter typeConverter) {
        log.trace("Adding type converter: {}", typeConverter);
        TypeMapping key = new TypeMapping(toType, fromType);
        synchronized (typeMappings) {
            TypeConverter converter = typeMappings.get(key);
            // only override it if its different
            // as race conditions can lead to many threads trying to promote the same fallback converter
            if (typeConverter != converter) {
                if (converter != null) {
                    log.warn("Overriding type converter from: " + converter + " to: " + typeConverter);
                }
                typeMappings.put(key, typeConverter);
                // remove any previous misses, as we added the new type converter
                misses.remove(key);
            }
        }
    }
    protected Object doConvertTo(final Class type, final Exchange exchange, final Object value) {
        if (log.isTraceEnabled()) {
            log.trace("Converting {} -> {} with value: {}",
                    new Object[]{value == null ? "null" : value.getClass().getCanonicalName(), 
                        type.getCanonicalName(), value});
        }

        if (value == null) {
            // lets avoid NullPointerException when converting to boolean for null values
            if (boolean.class.isAssignableFrom(type)) {
                return Boolean.FALSE;
            }
            return null;
        }

        // same instance type
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        // check if we have tried it before and if its a miss
        TypeMapping key = new TypeMapping(type, value.getClass());
        if (misses.containsKey(key)) {
            // we have tried before but we cannot convert this one
            return Void.TYPE;
        }

        // try to find a suitable type converter
        TypeConverter converter = getOrFindTypeConverter(type, value);
        if (converter != null) {
            log.trace("Using converter: {} to convert {}", converter, key);
            Object rc = converter.convertTo(type, exchange, value);
            if (rc != null) {
                return rc;
            }
        }

        // fallback converters
        for (FallbackTypeConverter fallback : fallbackConverters) {
            Object rc = fallback.getFallbackTypeConverter().convertTo(type, exchange, value);

            if (Void.TYPE.equals(rc)) {
                // it cannot be converted so give up
                return Void.TYPE;
            }

            if (rc != null) {
                // if fallback can promote then let it be promoted to a first class type converter
                if (fallback.isCanPromote()) {
                    // add it as a known type converter since we found a fallback that could do it
                    if (log.isDebugEnabled()) {
                        log.debug("Promoting fallback type converter as a known type converter to convert from: {} to: {} for the fallback converter: {}",
                                new Object[]{type.getCanonicalName(), value.getClass().getCanonicalName(), fallback.getFallbackTypeConverter()});
                    }
                    addTypeConverter(type, value.getClass(), fallback.getFallbackTypeConverter());
                }

                if (log.isTraceEnabled()) {
                    log.trace("Fallback type converter {} converted type from: {} to: {}",
                            new Object[]{fallback.getFallbackTypeConverter(),
                                type.getCanonicalName(), value.getClass().getCanonicalName()});
                }

                // return converted value
                return rc;
            }
        }

        // not found with that type then if it was a primitive type then try again with the wrapper type
        if (type.isPrimitive()) {
            Class primitiveType = ObjectHelper.convertPrimitiveTypeToWrapperType(type);
            if (primitiveType != type) {
                return convertTo(primitiveType, exchange, value);
            }
        }

        // Could not find suitable conversion, so remember it
        misses.put(key, key);

        // Could not find suitable conversion, so return Void to indicate not found
        return Void.TYPE;
    }
