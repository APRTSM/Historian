    private void deleteSubtreeWriter(SolrServer solrServer, String path)
            throws IOException, SolrServerException {
        // TODO verify the removal of the entire sub-hierarchy
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = path.replace("/", "\\/");
        solrServer.deleteByQuery(new StringBuilder(configuration.getPathField())
                .append(':').append(path).append("*").toString());
    }
