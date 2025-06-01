    PropertyState createMultiState(String name, int type, Value[] values, PropertyDefinition definition)
            throws RepositoryException {
        if (!definition.isMultiple()) {
            throw new ValueFormatException("Cannot set value array to single value property");
        }

        Value[] nonNullValues = compact(values);
        int targetType = getType(definition, type);
        if (nonNullValues.length == 0) {
            return MemoryPropertyBuilder
                    .array(Type.fromTag(type, false), name)
                    .getPropertyState();
        } else if (targetType == type) {
            return PropertyStates.createProperty(name, Arrays.asList(nonNullValues));
        } else {
            return PropertyStates.createProperty(name, Arrays.asList(ValueHelper.convert(
                    values, targetType, getValueFactory())));
        }
    }
