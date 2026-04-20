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
