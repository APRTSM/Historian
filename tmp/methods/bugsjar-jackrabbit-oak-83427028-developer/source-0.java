    private Tree findMatchingPropertyDefinition(
            List<Tree> types, String propertyName, Type<?> propertyType,
            boolean exactTypeMatch) {
        // Escape the property name for looking up a matching definition
        String escapedName;
        if (JCR_PRIMARYTYPE.equals(propertyName)) {
            escapedName = "oak:primaryType";
        } else if (JCR_MIXINTYPES.equals(propertyName)) {
            escapedName = "oak:mixinTypes";
        } else if (JCR_UUID.equals(propertyName)) {
            escapedName = "oak:uuid";
        } else {
            escapedName = propertyName;
        }

        String definedType = propertyType.toString();
        String undefinedType = UNDEFINED.toString();
        if (propertyType.isArray()) {
            undefinedType = UNDEFINEDS.toString();
        }

        // First look for a matching named property definition
        for (Tree type : types) {
            Tree definitions = type
                    .getChild(OAK_NAMED_PROPERTY_DEFINITIONS)
                    .getChild(escapedName);
            Tree definition = definitions.getChild(definedType);
            if (definition.exists()) {
                return definition;
            }
            definition = definitions.getChild(undefinedType);
            if (definition.exists()) {
                return definition;
            }
            if (!exactTypeMatch) {
                for (Tree def : definitions.getChildren()) {
                    if (propertyType.isArray() == TreeUtil.getBoolean(def, JCR_MULTIPLE)) {
                        return def;
                    }
                }
            }
        }

        // Then look through any residual property definitions
        for (Tree type : types) {
            Tree definitions = type.getChild(OAK_RESIDUAL_PROPERTY_DEFINITIONS);
            Tree definition = definitions.getChild(definedType);
            if (definition.exists()) {
                return definition;
            }
            definition = definitions.getChild(undefinedType);
            if (definition.exists()) {
                return definition;
            }
            if (!exactTypeMatch) {
                for (Tree def : definitions.getChildren()) {
                    if (propertyType.isArray() == TreeUtil.getBoolean(def, JCR_MULTIPLE)) {
                        return def;
                    }
                }
            }
        }

        return null;
    }
