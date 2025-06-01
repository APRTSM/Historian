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
        if (!serverName.startsWith("https://") && !serverName.startsWith("http://")) {
            String scheme = request.isSecure() ? "https://" : "http://";
            builder = new URIBuilder(scheme + serverName, encode);
        } else {
            builder = new URIBuilder(serverName, encode);
        }
