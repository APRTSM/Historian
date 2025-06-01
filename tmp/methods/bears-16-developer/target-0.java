    public <T> T reportBadTypeDefinition(BeanDescription bean,
            String msg, Object... msgArgs) throws JsonMappingException {
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        }
        msg = String.format("Invalid type definition for type %s: %s",
                beanDesc, _format(msg, msgArgs));
        throw InvalidDefinitionException.from(getGenerator(), msg, bean, null);
    }
    public <T> T reportBadPropertyDefinition(BeanDescription bean, BeanPropertyDefinition prop,
            String message, Object... msgArgs) throws JsonMappingException {
        message = _format(message, msgArgs);
        String propName = "N/A";
        if (prop != null) {
            propName = _quotedString(prop.getName());
        }
        String beanDesc = "N/A";
        if (bean != null) {
            beanDesc = ClassUtil.nameOf(bean.getBeanClass());
        }
        message = String.format("Invalid definition for property %s (of type %s): %s",
                propName, beanDesc, message);
        throw InvalidDefinitionException.from(getGenerator(), message, bean, prop);
    }
