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
        return evaluate(p1, p2);
    }
