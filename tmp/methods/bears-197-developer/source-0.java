    private static boolean serverNameContainsPort(final boolean containsScheme, final String serverName) {
        if (!containsScheme && serverName.contains(":")) {
            return true;
        }

        final int schemeIndex = serverName.indexOf(":");
        final int portIndex = serverName.lastIndexOf(":");
        return schemeIndex != portIndex;
    }
    public static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
            final String service, final String serverNames, final String serviceParameterName,
            final String artifactParameterName, final boolean encode) {
        if (CommonUtils.isNotBlank(service)) {
            return encode ? response.encodeURL(service) : service;
        }

        final String serverName = findMatchingServerName(request, serverNames);
        final URIBuilder originalRequestUrl = new URIBuilder(request.getRequestURL().toString(), encode);
        originalRequestUrl.setParameters(request.getQueryString());

        final URIBuilder builder;

        boolean containsScheme = true;
        if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
            builder = new URIBuilder(encode);
            builder.setScheme(request.isSecure() ? "https" : "http");
            builder.setHost(serverName);
            containsScheme = false;
        }  else {
            builder = new URIBuilder(serverName, encode);
        }
