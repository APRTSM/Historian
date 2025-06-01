    public PropertyValue currentProperty() {
        PropertyValue p = operand.currentProperty();
        if (p == null) {
            return null;
        }
        // TODO toLowerCase(): document the Turkish locale problem
        if (p.getType().isArray()) {
            Iterable<String> lowerCase = transform(p.getValue(STRINGS),
                    new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            return input.toLowerCase();
                        }
                    });
            return PropertyValues.newString(lowerCase);
        } else {
            String value = p.getValue(STRING);
            return PropertyValues.newString(value.toLowerCase());
        }
    }
