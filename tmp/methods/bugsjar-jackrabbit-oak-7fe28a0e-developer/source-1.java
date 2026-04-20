    public Property setProperty(String jcrName, Value value, int type)
            throws RepositoryException {
        checkStatus();

        int targetType = getTargetType(value, type);
        Value targetValue = ValueHelper.convert(value, targetType, getValueFactory());
        if (value == null) {
            Property p = getProperty(jcrName);
            p.remove();
            return p;
        } else {
            String oakName = sessionDelegate.getOakPathOrThrow(jcrName);
            CoreValue oakValue = ValueConverter.toCoreValue(targetValue, sessionDelegate);
            return new PropertyImpl(dlg.setProperty(oakName, oakValue));
        }
    }
