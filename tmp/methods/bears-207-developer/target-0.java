    public Response content(@PathParam("path") String path) {
        Response response;
        String mediaType = getMediaTypeFromPath(path);
        if (path.isEmpty() || path.equals("index.html")) {
            response = Response.ok().entity(getIndex()).type(mediaType).build();
        } else {
            mediaType = getMediaTypeFromPath(path);
            if (path.endsWith("png") || path.endsWith("gif")) {
                byte[] content = FileUtils.readAllBytesFromResource(swaggerResource + path);
                if (content != null) {
                    response = Response.ok().entity(content).type(mediaType).build();
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).build();
                }
            } else {
                String content = FileUtils.readAllTextFromResource(swaggerResource + path);
                if (content != null) {
                    response = Response.ok().entity(content).type(mediaType).build();
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).build();
                }
            }
        }
        return response;
    }
