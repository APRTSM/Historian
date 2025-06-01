    public Object getInjectionPropertyValue(Class<?> type, String propertyName, String propertyDefaultValue,
                                            String injectionPointName, Object bean, String beanName) {
        try {
            // enforce a properties component to be created if none existed
            CamelContextHelper.lookupPropertiesComponent(getCamelContext(), true);

            String key;
            String prefix = getCamelContext().getPropertyPrefixToken();
            String suffix = getCamelContext().getPropertySuffixToken();
            if (!propertyName.contains(prefix)) {
                // must enclose the property name with prefix/suffix to have it resolved
                key = prefix + propertyName + suffix;
            } else {
                // key has already prefix/suffix so use it as-is as it may be a compound key
                key = propertyName;
            }
            String value = getCamelContext().resolvePropertyPlaceholders(key);
            if (value != null) {
                return getCamelContext().getTypeConverter().mandatoryConvertTo(type, value);
            } else {
                return null;
            }
        } catch (Exception e) {
            if (ObjectHelper.isNotEmpty(propertyDefaultValue)) {
                try {
                    return getCamelContext().getTypeConverter().mandatoryConvertTo(type, propertyDefaultValue);
                } catch (Exception e2) {
                    throw ObjectHelper.wrapRuntimeCamelException(e2);
                }
            }
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
