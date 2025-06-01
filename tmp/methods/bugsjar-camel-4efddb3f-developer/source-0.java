    public static Map<String, Object> parseQuery(String uri) throws URISyntaxException {
        // must check for trailing & as the uri.split("&") will ignore those
        if (uri != null && uri.endsWith("&")) {
            throw new URISyntaxException(uri, "Invalid uri syntax: Trailing & marker found. "
                + "Check the uri and remove the trailing & marker.");
        }

        try {
            // use a linked map so the parameters is in the same order
            Map<String, Object> rc = new LinkedHashMap<String, Object>();
            if (uri != null) {
                String[] parameters = uri.split("&");
                for (String parameter : parameters) {
                    int p = parameter.indexOf("=");
                    if (p >= 0) {
                        String name = URLDecoder.decode(parameter.substring(0, p), CHARSET);
                        String value = URLDecoder.decode(parameter.substring(p + 1), CHARSET);
                        rc.put(name, value);
                    } else {
                        rc.put(parameter, null);
                    }
                }
            }
            return rc;
        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }
    public static String createQueryString(Map<Object, Object> options) throws URISyntaxException {
        try {
            if (options.size() > 0) {
                StringBuilder rc = new StringBuilder();
                boolean first = true;
                for (Object o : options.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        rc.append("&");
                    }

                    String key = (String) o;
                    String value = (String) options.get(key);
                    rc.append(URLEncoder.encode(key, CHARSET));
                    // only append if value is not null
                    if (value != null) {
                        rc.append("=");
                        rc.append(URLEncoder.encode(value, CHARSET));
                    }
                }
                return rc.toString();
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }
