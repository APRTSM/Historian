    public boolean evaluate() {
        // JCR 2.0 spec, 6.7.16 Comparison:
        // "operand1 may evaluate to an array of values"
        PropertyValue p1 = operand1.currentProperty();
        if (p1 == null) {
            return false;
        }
        PropertyValue p2 = operand2.currentValue();
        if (p2 == null) {
            // if the property doesn't exist, the result is always false
            // even for "null <> 'x'" (same as in SQL) 
            return false;
        }
        // "the value of operand2 is converted to the
        // property type of the value of operand1"
        try {
            p2 = convertValueToType(p2, p1);
        } catch (IllegalArgumentException ex) {
            // unable to convert, just skip this node
            return false;
        }
        if (p1.isArray()) {
            // JCR 2.0 spec, 6.7.16 Comparison:
            // "... constraint is satisfied as a whole if the comparison
            // against any element of the array is satisfied."
            Type<?> base = p1.getType().getBaseType();
            for (int i = 0; i < p1.count(); i++) {
                PropertyState value = PropertyStates.createProperty(
                        "value", p1.getValue(base, i), base);
                if (evaluate(PropertyValues.create(value), p2)) {
                    return true;
                }
            }
            return false;
        } else {
            return evaluate(p1, p2);
        }
    }
