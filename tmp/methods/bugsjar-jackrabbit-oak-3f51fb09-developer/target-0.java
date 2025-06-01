    public static PropertyState createProperty(String name, Value value) throws RepositoryException {

        int type = value.getType();
        switch (type) {
            case PropertyType.STRING:
                return StringPropertyState.stringProperty(name, getString(value, type));
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
                return new GenericPropertyState(name, getString(value, type), Type.fromTag(type, false));
        }
    }
    private static String getString(Value value, int type) throws RepositoryException {
        if (value instanceof ValueImpl) {
            return ((ValueImpl) value).getOakString();
        }
        else if (type == PropertyType.NAME || type == PropertyType.PATH) {
            throw new IllegalArgumentException("Cannot create name of path property state from Value " +
                    "of class '" + value.getClass() + '\'');
        } else {
            return value.getString();
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
                    strings.add(getString(value, type));
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
                    vals.add(getString(value, type));
                }
                return new MultiGenericPropertyState(name, vals, Type.fromTag(type, true));
        }
    }
