    public Property setProperty(final String jcrName, final Value value, final int type)
            throws RepositoryException {
        checkStatus();

        return sessionDelegate.perform(new SessionOperation<PropertyImpl>() {
            @Override
            public PropertyImpl perform() throws RepositoryException {
                String oakName = sessionDelegate.getOakPathOrThrow(jcrName);
                if (value == null) {
                    dlg.removeProperty(oakName);
                    return null;
                } else {
                    int targetType = getTargetType(value, type);
                    Value targetValue =
                            ValueHelper.convert(value, targetType, getValueFactory());
                    return new PropertyImpl(dlg.setProperty(oakName, targetValue));
                }
            }
        });
    }
