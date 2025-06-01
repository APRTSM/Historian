    private void initAttributes(final AbstractAttribute... attributes) {

        if (attributes == null || attributes.length == 0) {
            return;
        }

        attributesMap = new ConcurrentHashMap<>();

        for (final AbstractAttribute attribute : attributes) {
            attributesMap.put(attribute.getAttributeName(), attribute);
        }

        this.attributes = new AbstractAttribute[attributesMap.size()];
        attributesMap.values().toArray(this.attributes);
    }
