    private Property internalSetProperty(final String jcrName, final Value value,
                                         final int type, final boolean exactTypeMatch) throws RepositoryException {
        checkStatus();
        checkProtected();

        return sessionDelegate.perform(new SessionOperation<Property>() {
            @Override
            public Property perform() throws RepositoryException {
                if (value == null) {
                    Property property = getProperty(jcrName);
                    property.remove();
                    return property;
                } else {
                    String oakName = sessionDelegate.getOakPathOrThrow(jcrName);

                    PropertyDefinition definition;
                    if (hasProperty(jcrName)) {
                        definition = getProperty(jcrName).getDefinition();
                    } else {
                        definition = dlg.sessionDelegate.getDefinitionProvider().getDefinition(NodeImpl.this, oakName, false, type, exactTypeMatch);
                    }
                    checkProtected(definition);
                    if (definition.isMultiple()) {
                        throw new ValueFormatException("Cannot set single value to multivalued property");
                    }

                    int targetType = getTargetType(value, definition);
                    Value targetValue = ValueHelper.convert(value, targetType, getValueFactory());

                    return new PropertyImpl(dlg.setProperty(oakName, targetValue));
                }
            }
        });
    }
    private Property internalSetProperty(final String jcrName, final Value[] values,
                                         final int type, final boolean exactTypeMatch) throws RepositoryException {
        checkStatus();
        checkProtected();

        return sessionDelegate.perform(new SessionOperation<Property>() {
            @Override
            public Property perform() throws RepositoryException {
                if (values == null) {
                    Property p = getProperty(jcrName);
                    p.remove();
                    return p;
                } else {
                    String oakName = sessionDelegate.getOakPathOrThrow(jcrName);

                    PropertyDefinition definition;
                    if (hasProperty(jcrName)) {
                        definition = getProperty(jcrName).getDefinition();
                    } else {
                        definition = dlg.sessionDelegate.getDefinitionProvider().getDefinition(NodeImpl.this, oakName, true, type, exactTypeMatch);
                    }
                    checkProtected(definition);
                    if (!definition.isMultiple()) {
                        throw new ValueFormatException("Cannot set value array to single value property");
                    }

                    int targetType = getTargetType(values, definition);
                    Value[] targetValues = ValueHelper.convert(values, targetType, getValueFactory());

                    Iterable<Value> nonNullValues = Iterables.filter(
                            Arrays.asList(targetValues),
                            Predicates.notNull());
                    return new PropertyImpl(dlg.setProperty(oakName, nonNullValues));
                }
            }
        });
    }
