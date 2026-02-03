    public String getNamespaceURI(String prefix) {
	prefix = prefix.toLowerCase();

        return namespaceResolver.getNamespaceURI(prefix);
    }
