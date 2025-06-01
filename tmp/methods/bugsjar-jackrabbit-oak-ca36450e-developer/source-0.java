    public PropertyValue currentProperty() {
        PropertyValue p = operand.currentProperty();
        if (p == null) {
            return null;
        }
        // TODO what is the expected result of LOWER(x) for an array property?
        // currently throws an exception
        String value = p.getValue(STRING);
        // TODO toLowerCase(): document the Turkish locale problem
        return PropertyValues.newString(value.toLowerCase());
    }
