    private boolean addTypedOrderedFields(List<Field> fields,
                                          PropertyState property,
                                          String pname,
                                          PropertyDefinition pd) throws CommitFailedException {
        // Ignore and warn if property multi-valued as not supported
        if (property.getType().isArray()) {
            log.warn(
                "Ignoring ordered property {} of type {} for path {} as multivalued ordered property not supported",
                pname, Type.fromTag(property.getType().tag(), true), getPath());
            return false;
        }

        int tag = property.getType().tag();
        int idxDefinedTag = pd.getType();
        // Try converting type to the defined type in the index definition
        if (tag != idxDefinedTag) {
            log.debug(
                "Ordered property defined with type {} differs from property {} with type {} in " +
                    "path {}",
                Type.fromTag(idxDefinedTag, false), property.toString(), Type.fromTag(tag, false),
                getPath());
            tag = idxDefinedTag;
        }

        String name = FieldNames.createDocValFieldName(pname);
        boolean fieldAdded = false;
        Field f = null;
        try {
            if (tag == Type.LONG.tag()) {
                //TODO Distinguish fields which need to be used for search and for sort
                //If a field is only used for Sort then it can be stored with less precision
                f = new NumericDocValuesField(name, property.getValue(Type.LONG));
            } else if (tag == Type.DATE.tag()) {
                String date = property.getValue(Type.DATE);
                f = new NumericDocValuesField(name, FieldFactory.dateToLong(date));
            } else if (tag == Type.DOUBLE.tag()) {
                f = new DoubleDocValuesField(name, property.getValue(Type.DOUBLE));
            } else if (tag == Type.BOOLEAN.tag()) {
                f = new SortedDocValuesField(name,
                    new BytesRef(property.getValue(Type.BOOLEAN).toString()));
            } else if (tag == Type.STRING.tag()) {
                f = new SortedDocValuesField(name,
                    new BytesRef(property.getValue(Type.STRING)));
            }

            if (f != null) {
                fields.add(f);
                fieldAdded = true;
            }
        } catch (Exception e) {
            log.warn(
                "Ignoring ordered property. Could not convert property {} of type {} to type " +
                    "{} for path {}",
                pname, Type.fromTag(property.getType().tag(), false),
                Type.fromTag(tag, false), getPath(), e);
        }
        return fieldAdded;
    }
