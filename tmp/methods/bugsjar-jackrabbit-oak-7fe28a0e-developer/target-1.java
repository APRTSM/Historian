    public void removeProperty(String name) throws InvalidItemStateException {
        getTree().removeProperty(name);
    }
    public Property setProperty(String jcrName, Value value, int type)
            throws RepositoryException {
        checkStatus();

        String oakName = sessionDelegate.getOakPathOrThrow(jcrName);
        if (value == null) {
            dlg.removeProperty(oakName);
            return null;
        } else {
            int targetType = getTargetType(value, type);
            Value targetValue =
                    ValueHelper.convert(value, targetType, getValueFactory());
            CoreValue oakValue =
                    ValueConverter.toCoreValue(targetValue, sessionDelegate);
            return new PropertyImpl(dlg.setProperty(oakName, oakValue));
        }
    }
