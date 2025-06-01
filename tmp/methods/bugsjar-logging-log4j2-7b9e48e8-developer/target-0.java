    public static File fileFromURI(URI uri) {
        if (uri == null || (uri.getScheme() != null &&
            (!PROTOCOL_FILE.equals(uri.getScheme()) && !JBOSS_FILE.equals(uri.getScheme())))) {
            return null;
        }
        if (uri.getScheme() == null) {
            try {
                uri = new File(uri.getPath()).toURI();
            } catch (final Exception ex) {
                LOGGER.warn("Invalid URI " + uri);
                return null;
            }
        }
        try {
            String fileName = uri.toURL().getFile();
            if (new File(fileName).exists()) {
                return new File(fileName);
            }
            return new File(URLDecoder.decode(fileName, "UTF8"));
        } catch (final MalformedURLException ex) {
            LOGGER.warn("Invalid URL " + uri, ex);
        } catch (final UnsupportedEncodingException uee) {
            LOGGER.warn("Invalid encoding: UTF8", uee);
        }
        return null;
    }
