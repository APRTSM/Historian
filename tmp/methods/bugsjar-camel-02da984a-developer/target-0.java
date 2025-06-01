    public void setPropertySuffix(String propertySuffix) {
        this.propertySuffix = propertySuffix;
        if (ObjectHelper.isNotEmpty(this.propertySuffix)) {
            this.propertySuffix = FilePathResolver.resolvePath(this.propertySuffix);
        }
    }
    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
        if (ObjectHelper.isNotEmpty(this.propertyPrefix)) {
            this.propertyPrefix = FilePathResolver.resolvePath(this.propertyPrefix);
        }
    }
