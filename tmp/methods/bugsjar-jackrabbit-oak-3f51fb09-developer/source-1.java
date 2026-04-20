    public static PropertyState createProperty(String name, Value value) throws RepositoryException {
        int type = value.getType();
        switch (type) {
            case PropertyType.STRING:
                return StringPropertyState.stringProperty(name, value.getString());
            case PropertyType.BINARY:
                return BinaryPropertyState.binaryProperty(name, value);
            case PropertyType.LONG:
                return LongPropertyState.createLongProperty(name, value.getLong());
            case PropertyType.DOUBLE:
                return DoublePropertyState.doubleProperty(name, value.getDouble());
            case PropertyType.DATE:
                return LongPropertyState.createDateProperty(name, value.getLong());
            case PropertyType.BOOLEAN:
                return BooleanPropertyState.booleanProperty(name, value.getBoolean());
            case PropertyType.DECIMAL:
                return DecimalPropertyState.decimalProperty(name, value.getDecimal());
            default:
                return new GenericPropertyState(name, value.getString(), Type.fromTag(type, false));
        }
    }
    public static PropertyState createProperty(String name, Iterable<Value> values) throws RepositoryException {
        Value first = Iterables.getFirst(values, null);
        if (first == null) {
            return EmptyPropertyState.emptyProperty(name, STRINGS);
        }

        int type = first.getType();
        switch (type) {
            case PropertyType.STRING:
                List<String> strings = Lists.newArrayList();
                for (Value value : values) {
                    strings.add(value.getString());
                }
                return MultiStringPropertyState.stringProperty(name, strings);
            case PropertyType.BINARY:
                List<Blob> blobs = Lists.newArrayList();
                for (Value value : values) {
                    blobs.add(new ValueBasedBlob(value));
                }
                return MultiBinaryPropertyState.binaryPropertyFromBlob(name, blobs);
            case PropertyType.LONG:
                List<Long> longs = Lists.newArrayList();
                for (Value value : values) {
                    longs.add(value.getLong());
                }
                return MultiLongPropertyState.createLongProperty(name, longs);
            case PropertyType.DOUBLE:
                List<Double> doubles = Lists.newArrayList();
                for (Value value : values) {
                    doubles.add(value.getDouble());
                }
                return MultiDoublePropertyState.doubleProperty(name, doubles);
            case PropertyType.DATE:
                List<Long> dates = Lists.newArrayList();
                for (Value value : values) {
                    dates.add(value.getLong());
                }
                return MultiLongPropertyState.createDatePropertyFromLong(name, dates);
            case PropertyType.BOOLEAN:
                List<Boolean> booleans = Lists.newArrayList();
                for (Value value : values) {
                    booleans.add(value.getBoolean());
                }
                return MultiBooleanPropertyState.booleanProperty(name, booleans);
            case PropertyType.DECIMAL:
                List<BigDecimal> decimals = Lists.newArrayList();
                for (Value value : values) {
                    decimals.add(value.getDecimal());
                }
                return MultiDecimalPropertyState.decimalProperty(name, decimals);
            default:
                List<String> vals = Lists.newArrayList();
                for (Value value : values) {
                    vals.add(value.getString());
                }
                return new MultiGenericPropertyState(name, vals, Type.fromTag(type, true));
        }
    }
    public String getString() throws RepositoryException {
        checkState(getType() != PropertyType.BINARY || stream == null,
                "getStream has previously been called on this Value instance. " +
                "In this case a new Value instance must be acquired in order to successfully call this method.");

        switch (getType()) {
            case PropertyType.NAME:
                return namePathMapper.getJcrName(propertyState.getValue(Type.STRING, index));
            case PropertyType.PATH:
                String s = propertyState.getValue(Type.STRING, index);
                if (s.startsWith("[") && s.endsWith("]")) {
                    // identifier paths are returned as-is (JCR 2.0, 3.4.3.1)
                    return s;
                } else {
                    return namePathMapper.getJcrPath(s);
                }
            default:
                return propertyState.getValue(Type.STRING, index);
        }
    }
    public String toString() {
        return propertyState.getValue(Type.STRING, index);
    }
    public int hashCode() {
        if (getType() == PropertyType.BINARY) {
            return propertyState.getValue(Type.BINARY, index).hashCode();
        }
        else {
            return propertyState.getValue(Type.STRING, index).hashCode();
        }
    }
