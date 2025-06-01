    public <T> T reportBadTypeDefinition(BeanDescription bean,
            String msg, Object... msgArgs) throws JsonMappingException {
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = _desc(bean.getType().getGenericSignature());
        }
        msg = String.format("Invalid type definition for type %s: %s",
                beanDesc, _format(msg, msgArgs));
        throw InvalidDefinitionException.from(getGenerator(), msg, bean, null);
    }
    public <T> T reportBadPropertyDefinition(BeanDescription bean, BeanPropertyDefinition prop,
            String message, Object... msgArgs) throws JsonMappingException {
        message = _format(message, msgArgs);
        String propName = "N/A";
        if (prop != null) {
            propName = _quotedString(prop.getName());
        }
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = _desc(bean.getType().getGenericSignature());
        }
        message = String.format("Invalid definition for property %s (of type %s): %s",
                propName, beanDesc, message);
        throw InvalidDefinitionException.from(getGenerator(), message, bean, prop);
    }
    public JsonSerializer<?> build()
    {
        BeanPropertyWriter[] properties;
        // No properties, any getter or object id writer?
        // No real serializer; caller gets to handle
        if (_properties == null || _properties.isEmpty()) {
            if (_anyGetter == null && _objectIdWriter == null) {
                return null;
            }
            properties = NO_PROPERTIES;
        } else {
            properties = _properties.toArray(new BeanPropertyWriter[_properties.size()]);
            if (_config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                for (int i = 0, end = properties.length; i < end; ++i) {
                    properties[i].fixAccess(_config);
                }
            }
        }
        if (_anyGetter != null) {
            _anyGetter.fixAccess(_config);
        }
        if (_typeId != null) {
            if (_config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                _typeId.fixAccess(_config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        return new BeanSerializer(_beanDesc.getType(), this,
                properties, _filteredProperties);
    }
