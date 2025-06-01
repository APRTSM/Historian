    private void fixFrozenUuid() {
        // OAK-1789: Convert the jcr:frozenUuid of a non-referenceable
        // frozen node from UUID to a path identifier
        PropertyState frozenUuid = properties.get(JCR_FROZENUUID);
        if (frozenUuid != null
                && frozenUuid.getType() == STRING
                && isFrozenNode.apply(this)) {
            String frozenPrimary = NT_BASE;
            Set<String> frozenMixins = newHashSet();

            PropertyState property = properties.get(JCR_FROZENPRIMARYTYPE);
            if (property != null && property.getType() == NAME) {
                frozenPrimary = property.getValue(NAME);
            }
            property = properties.get(JCR_FROZENMIXINTYPES);
            if (property != null && property.getType() == NAMES) {
                addAll(frozenMixins, property.getValue(NAMES));
            }

            if (!isReferenceable.apply(frozenPrimary, frozenMixins)) {
                String parentFrozenUuid = parent.getString(JCR_FROZENUUID);
                if (parentFrozenUuid != null) {
                    frozenUuid = PropertyStates.createProperty(
                            JCR_FROZENUUID, parentFrozenUuid + "/" + name);
                    properties.put(JCR_FROZENUUID, frozenUuid);
                }
            }
        }
    }
