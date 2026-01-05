    public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
             copySafeNodes(dirtyDocument, clean.body());

        return clean;
    }
