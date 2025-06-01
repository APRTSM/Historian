    public PropertyState getProperty(String name) {
        checkNotNull(name);
        Template template = getTemplate();
        PropertyState property = null;
        if (JCR_PRIMARYTYPE.equals(name)) {
            property = template.getPrimaryType();
        } else if (JCR_MIXINTYPES.equals(name)) {
            property = template.getMixinTypes();
        }
        if (property != null) {
            return property;
        }

        PropertyTemplate propertyTemplate =
                template.getPropertyTemplate(name);
        if (propertyTemplate != null) {
            Segment segment = getSegment();
            int ids = 1 + propertyTemplate.getIndex();
            if (template.getChildName() != Template.ZERO_CHILD_NODES) {
                ids++;
            }
            return new SegmentPropertyState(
                    segment.readRecordId(getOffset(0, ids)), propertyTemplate);
        } else {
            return null;
        }
    }
