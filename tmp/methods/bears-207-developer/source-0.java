    public Response content(@PathParam("path") String path) {
        Response response;
        String mediaType = getMediaTypeFromPath(path);
        if (path.isEmpty() || path.equals("index.html")) {
            response = Response.ok().entity(getIndex()).type(mediaType).build();
        } else {
            mediaType = getMediaTypeFromPath(path);
            if (path.endsWith("png") || path.endsWith("gif")) {
                byte[] content = FileUtils.readAllBytesFromResource(swaggerResource + path);
                response = Response.ok().entity(content).type(mediaType).build();
            } else {
                String content = FileUtils.readAllTextFromResource(swaggerResource + path);
                response = Response.ok().entity(content).type(mediaType).build();
            }
        }
        return response;
    }
