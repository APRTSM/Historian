    private org.apache.jackrabbit.oak.api.PropertyState getProperty(
            String name, InternalValue value, int type)
            throws RepositoryException, IOException {
        switch (type) {
        case PropertyType.BINARY:
            return PropertyStates.createProperty(
                    name, store.createBlob(value.getStream()), Type.BINARY);
        case PropertyType.BOOLEAN:
            return PropertyStates.createProperty(
                    name, value.getBoolean(), Type.BOOLEAN);
        case PropertyType.DATE:
            return PropertyStates.createProperty(
                    name, value.getString(), Type.DATE);
        case PropertyType.DECIMAL:
            return PropertyStates.createProperty(
                    name, value.getDecimal(), Type.DECIMAL);
        case PropertyType.DOUBLE:
            return PropertyStates.createProperty(
                    name, value.getDouble(), Type.DOUBLE);
        case PropertyType.LONG:
            return PropertyStates.createProperty(
                    name, value.getLong(), Type.LONG);
        case PropertyType.NAME:
            return PropertyStates.createProperty(
                    name, getOakName(value.getName()), Type.NAME);
        case PropertyType.PATH:
            return PropertyStates.createProperty(
                    name, getOakPath(value.getPath()), Type.PATH);
        case PropertyType.REFERENCE:
            return PropertyStates.createProperty(
                    name, value.getNodeId().toString(), Type.REFERENCE);
        case PropertyType.STRING:
            return PropertyStates.createProperty(
                    name, value.getString(), Type.STRING);
        case PropertyType.URI:
            return PropertyStates.createProperty(
                    name, value.getURI().toString(), Type.URI);
        case PropertyType.WEAKREFERENCE:
            return PropertyStates.createProperty(
                    name, value.getNodeId().toString(), Type.WEAKREFERENCE);
        default:
            throw new RepositoryException("Unknown value type: " + type);
        }
    }
