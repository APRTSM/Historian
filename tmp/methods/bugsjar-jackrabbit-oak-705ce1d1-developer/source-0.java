    public PropertyState getProperty(String name) {
        checkNotNull(name);
        Template template = getTemplate();
        if (JCR_PRIMARYTYPE.equals(name)) {
            return template.getPrimaryType();
        } else if (JCR_MIXINTYPES.equals(name)) {
            return template.getMixinTypes();
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
