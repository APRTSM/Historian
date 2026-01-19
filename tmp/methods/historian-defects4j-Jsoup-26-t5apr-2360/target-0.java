    public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
     if (dirtyDocument.body() == null) { return clean; }
            copySafeNodes(dirtyDocument.body(), clean.body());

        return clean;
    }
