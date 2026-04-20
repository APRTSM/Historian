    public Value createValue(String value, int type) throws ValueFormatException {

        if (value == null) {
            throw new ValueFormatException();
        }

        try {
            CoreValue cv;

            switch (type) {
                case PropertyType.NAME:
                    String oakName = namePathMapper.getOakName(value);
                    if (oakName == null) {
                        throw new ValueFormatException("Invalid name: " + value);
                    }
                    cv = factory.createValue(oakName, type);
                    break;

                case PropertyType.PATH:
                    // TODO we special case identifier paths here for now
                    // eventually this should be done in the path mapper (OAK-23)

                    String oakValue;
                    if (value.startsWith("[") && value.endsWith("]")) {
                        oakValue = value;
                    } else {
                        oakValue = namePathMapper.getOakPath(value);
                        if (oakValue == null) {
                            throw new ValueFormatException("Invalid path: " + value);
                        }
                    }
                    cv = factory.createValue(oakValue, type);
                    break;

                case PropertyType.DATE:
                    if (ISO8601.parse(value) == null) {
                        throw new ValueFormatException("Invalid date " + value);
                    }
                    cv = factory.createValue(value, type);
                    break;

                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE:
                    // TODO: move to identifier/uuid management utility instead of relying on impl specific uuid-format here.
                    try {
                        UUID.fromString(value);
                    } catch (IllegalArgumentException e) {
                        throw new ValueFormatException(e);
                    }
                    cv = factory.createValue(value, type);
                    break;

                case PropertyType.BINARY:
                    cv = factory.createValue(new ByteArrayInputStream(value.getBytes("UTF-8")));
                    break;

                default:
                    cv = factory.createValue(value, type);
                    break;
            }

            return new ValueImpl(cv, namePathMapper);
        } catch (UnsupportedEncodingException e) {
            throw new ValueFormatException("Encoding UTF-8 not supported (this should not happen!)", e);
        } catch (IOException e) {
            throw new ValueFormatException(e);
        } catch (NumberFormatException e) {
            throw new ValueFormatException("Invalid value " + value + " for type " + PropertyType.nameFromValue(type));
        }
    }
