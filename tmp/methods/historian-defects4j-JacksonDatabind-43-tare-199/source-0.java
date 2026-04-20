    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        /* 09-Dec-2014, tatu: As per [#622], we need to allow Object Id references
         *   to come in as JSON Objects as well; but for now assume they will
         *   be simple, single-property references, which means that we can
         *   recognize them without having to buffer anything.
         *   Once again, if we must, we can do more complex handling with buffering,
         *   but let's only do that if and when that becomes necessary.
         */
        if (_objectIdReader != null && _objectIdReader.maySerializeAsObject()) {
            if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)
                    && _objectIdReader.isValidReferencePropertyName(p.getCurrentName(), p)) {
                return deserializeFromObjectId(p, ctxt);
            }
        }
        if (_nonStandardCreation) {
            if (_unwrappedPropertyHandler != null) {
                return deserializeWithUnwrapped(p, ctxt);
            }
            if (_externalTypeIdHandler != null) {
                return deserializeWithExternalTypeId(p, ctxt);
            }
            Object bean = deserializeFromObjectUsingNonDefault(p, ctxt);
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            /* 27-May-2014, tatu: I don't think view processing would work
             *   at this point, so commenting it out; but leaving in place
             *   just in case I forgot something fundamental...
             */
            /*
            if (_needViewProcesing) {
                Class<?> view = ctxt.getActiveView();
                if (view != null) {
                    return deserializeWithView(p, ctxt, bean, view);
                }
            }
            */
            return bean;
        }
        final Object bean = _valueInstantiator.createUsingDefault(ctxt);
        // [databind#631]: Assign current value, to be accessible by custom deserializers
        p.setCurrentValue(bean);
        if (p.canReadObjectId()) {
            Object id = p.getObjectId();
            if (id != null) {
                _handleTypedObjectId(p, ctxt, bean, id);
            }
        }
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        if (_needViewProcesing) {
            Class<?> view = ctxt.getActiveView();
            if (view != null) {
                return deserializeWithView(p, ctxt, bean, view);
            }
        }
        if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
            String propName = p.getCurrentName();
            do {
                p.nextToken();
                SettableBeanProperty prop = _beanProperties.find(propName);
                if (prop != null) { // normal case
                    try {
                        prop.deserializeAndSet(p, ctxt, bean);
                    } catch (Exception e) {
                        wrapAndThrow(e, bean, propName, ctxt);
                    }
                    continue;
                }
                handleUnknownVanilla(p, ctxt, bean, propName);
            } while ((propName = p.nextFieldName()) != null);
        }
        return bean;
    }
