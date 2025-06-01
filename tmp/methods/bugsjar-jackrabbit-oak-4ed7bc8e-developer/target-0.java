    public Property setProperty(final String jcrName, final Value value, final int type)
            throws RepositoryException {
        checkStatus();

        return sessionDelegate.perform(new SessionOperation<Property>() {
            @Override
            public Property perform() throws RepositoryException {
                if (value == null) {
                    Property property = getProperty(jcrName);
                    property.remove();
                    return property;
                } else {
                    String oakName = sessionDelegate.getOakPathOrThrow(jcrName);
                    int targetType = getTargetType(value, type);
                    Value targetValue =
                            ValueHelper.convert(value, targetType, getValueFactory());
                    return new PropertyImpl(dlg.setProperty(oakName, targetValue));
                }
            }
        });
    }
