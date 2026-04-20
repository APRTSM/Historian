    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty("0" + name);
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }
