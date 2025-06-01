    PropertyState createMultiState(String name, int type, Value[] values, PropertyDefinition definition)
            throws RepositoryException {
        if (!definition.isMultiple()) {
            throw new ValueFormatException("Cannot set value array to single value property");
        }

        Value[] nonNullValues = compact(values);
        int targetType = getType(definition, type);
        if (nonNullValues.length == 0) {
            if (targetType == PropertyType.UNDEFINED) {
                // default to string when no other type hints are available
                targetType = PropertyType.STRING;
            }
            return MemoryPropertyBuilder
                    .array(Type.fromTag(targetType, false), name)
                    .getPropertyState();
        } else if (targetType == type) {
            return PropertyStates.createProperty(name, Arrays.asList(nonNullValues));
        } else {
            return PropertyStates.createProperty(name, Arrays.asList(ValueHelper.convert(
                    values, targetType, getValueFactory())));
        }
    }
