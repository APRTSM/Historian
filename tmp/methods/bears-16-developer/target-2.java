    public <T> T reportBadTypeDefinition(BeanDescription bean,
            String msg, Object... msgArgs) throws JsonMappingException {
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
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
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        }
        message = String.format("Invalid definition for property %s (of type %s): %s",
                propName, beanDesc, message);
        throw InvalidDefinitionException.from(getGenerator(), message, bean, prop);
    }
    public void setFilteredProperties(BeanPropertyWriter[] properties) {
        if (properties != null) {
            if (properties.length != _properties.size()) { // as per [databind#1612]
                throw new IllegalArgumentException(String.format(
                        "Trying to set %d filtered properties; must match length of non-filtered `properties` (%d)",
                        properties.length, _properties.size()));
            }
        }
        _filteredProperties = properties;
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
        // 27-Apr-2017, tatu: Verify that filtered-properties settings are compatible
        if (_filteredProperties != null) {
            if (_filteredProperties.length != _properties.size()) {
                throw new IllegalStateException(String.format(
"Mismatch between `properties` size (%d), `filteredProperties` (%s): should have as many (or `null` for latter)",
_properties.size(), _filteredProperties.length));
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
    protected JsonSerializer<Object> constructBeanSerializer(SerializerProvider prov,
            BeanDescription beanDesc)
        throws JsonMappingException
    {
        // 13-Oct-2010, tatu: quick sanity check: never try to create bean serializer for plain Object
        // 05-Jul-2012, tatu: ... but we should be able to just return "unknown type" serializer, right?
        if (beanDesc.getBeanClass() == Object.class) {
            return prov.getUnknownTypeSerializer(Object.class);
//            throw new IllegalArgumentException("Can not create bean serializer for Object.class");
        }
        final SerializationConfig config = prov.getConfig();
        BeanSerializerBuilder builder = constructBeanSerializerBuilder(beanDesc);
        builder.setConfig(config);

        // First: any detectable (auto-detect, annotations) properties to serialize?
        List<BeanPropertyWriter> props = findBeanProperties(prov, beanDesc, builder);
        if (props == null) {
            props = new ArrayList<BeanPropertyWriter>();
        } else {
            props = removeOverlappingTypeIds(prov, beanDesc, builder, props);
        }
        
        // [databind#638]: Allow injection of "virtual" properties:
        prov.getAnnotationIntrospector().findAndAddVirtualProperties(config, beanDesc.getClassInfo(), props);

        // [JACKSON-440] Need to allow modification bean properties to serialize:
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.changeProperties(config, beanDesc, props);
            }
        }

        // Any properties to suppress?
        props = filterBeanProperties(config, beanDesc, props);

        // Need to allow reordering of properties to serialize
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                props = mod.orderProperties(config, beanDesc, props);
            }
        }

        /* And if Object Id is needed, some preparation for that as well: better
         * do before view handling, mostly for the custom id case which needs
         * access to a property
         */
        builder.setObjectIdWriter(constructObjectIdHandler(prov, beanDesc, props));
        
        builder.setProperties(props);
        builder.setFilterId(findFilterId(config, beanDesc));

        AnnotatedMember anyGetter = beanDesc.findAnyGetter();
        if (anyGetter != null) {
            JavaType type = anyGetter.getType();
            // copied from BasicSerializerFactory.buildMapSerializer():
            boolean staticTyping = config.isEnabled(MapperFeature.USE_STATIC_TYPING);
            JavaType valueType = type.getContentType();
            TypeSerializer typeSer = createTypeSerializer(config, valueType);
            // last 2 nulls; don't know key, value serializers (yet)
            // 23-Feb-2015, tatu: As per [databind#705], need to support custom serializers
            JsonSerializer<?> anySer = findSerializerFromAnnotation(prov, anyGetter);
            if (anySer == null) {
                // TODO: support '@JsonIgnoreProperties' with any setter?
                anySer = MapSerializer.construct(/* ignored props*/ (Set<String>) null,
                        type, staticTyping, typeSer, null, null, /*filterId*/ null);
            }
            // TODO: can we find full PropertyName?
            PropertyName name = PropertyName.construct(anyGetter.getName());
            BeanProperty.Std anyProp = new BeanProperty.Std(name, valueType, null,
                    anyGetter, PropertyMetadata.STD_OPTIONAL);
            builder.setAnyGetter(new AnyGetterWriter(anyProp, anyGetter, anySer));
        }
        // Next: need to gather view information, if any:
        processViews(config, builder);

        // Finally: let interested parties mess with the result bit more...
        if (_factoryConfig.hasSerializerModifiers()) {
            for (BeanSerializerModifier mod : _factoryConfig.serializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }

        JsonSerializer<Object> ser = null;
        try {
            ser = (JsonSerializer<Object>) builder.build();
        } catch (RuntimeException e) {
            prov.reportBadTypeDefinition(beanDesc, "Failed to construct BeanSerializer for %s: (%s) %s",
                    beanDesc.getType(), e.getClass().getName(), e.getMessage());
        }
        if (ser == null) {
            // If we get this far, there were no properties found, so no regular BeanSerializer
            // would be constructed. But, couple of exceptions.
            // First: if there are known annotations, just create 'empty bean' serializer
            if (beanDesc.hasKnownClassAnnotations()) {
                return builder.createDummy();
            }
        }
        return ser;
    }
